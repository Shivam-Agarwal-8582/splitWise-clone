package com.splitwise.modules.user.service;

import com.splitwise.common.exception.ResourceNotFoundException;
import com.splitwise.modules.user.dto.UpdateProfileRequest;
import com.splitwise.modules.user.dto.UserDTO;
import com.splitwise.modules.user.model.User;
import com.splitwise.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return UserDTO.fromEntity(user);
    }

    @Transactional
    public UserDTO updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setFullName(request.getFullName().trim());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber().trim());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        if (request.getDefaultCurrency() != null) user.setDefaultCurrency(request.getDefaultCurrency());

        user = userRepository.save(user);
        return UserDTO.fromEntity(user);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        return userRepository.searchUsers(query.trim()).stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }
}

