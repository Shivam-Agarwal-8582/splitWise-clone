package com.splitwise.modules.user.controller;

import com.splitwise.common.response.ApiResponse;
import com.splitwise.modules.user.dto.UpdateProfileRequest;
import com.splitwise.modules.user.dto.UserDTO;
import com.splitwise.modules.user.service.UserService;
import com.splitwise.security.user.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserDTO user = userService.getUserProfile(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserDTO user = userService.updateProfile(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", user));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserDTO>>> searchUsers(@RequestParam("query") String query) {
        List<UserDTO> users = userService.searchUsers(query);
        return ResponseEntity.ok(ApiResponse.success(users));
    }
}

