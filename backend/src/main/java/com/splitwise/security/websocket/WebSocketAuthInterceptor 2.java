package com.splitwise.security.websocket;

import com.splitwise.modules.group.repository.GroupMemberRepository;
import com.splitwise.security.jwt.JwtTokenProvider;
import com.splitwise.security.user.CustomUserDetails;
import com.splitwise.security.user.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final GroupMemberRepository groupMemberRepository;

    private static final Pattern GROUP_DESTINATION_PATTERN = Pattern.compile("^/(topic|app)/groups/(\\d+)(/.*)?$");

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        // 1. Authenticate on CONNECT frame
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (tokenProvider.validateToken(token)) {
                    Long userId = tokenProvider.getUserIdFromToken(token);
                    UserDetails userDetails = userDetailsService.loadUserById(userId);
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    accessor.setUser(auth);
                    log.info("WebSocket connection authenticated for user: {}", userDetails.getUsername());
                } else {
                    log.warn("Invalid JWT token during WebSocket handshake");
                    throw new AccessDeniedException("Invalid or expired JWT token");
                }
            } else {
                log.warn("Missing Authorization header on WebSocket CONNECT");
                throw new AccessDeniedException("Missing Authorization header");
            }
        }

        // 2. Validate group authorization on SUBSCRIBE or SEND frames
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand()) || StompCommand.SEND.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            if (destination != null) {
                Matcher matcher = GROUP_DESTINATION_PATTERN.matcher(destination);
                if (matcher.matches()) {
                    Long groupId = Long.parseLong(matcher.group(2));
                    if (accessor.getUser() instanceof UsernamePasswordAuthenticationToken auth
                            && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
                        Long currentUserId = userDetails.getId();
                        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, currentUserId)) {
                            log.warn("User {} attempted unauthorized access to group {} on destination {}",
                                    currentUserId, groupId, destination);
                            throw new AccessDeniedException("You are not authorized to access group " + groupId);
                        }
                    } else {
                        throw new AccessDeniedException("Unauthenticated WebSocket request");
                    }
                }
            }
        }

        return message;
    }
}

