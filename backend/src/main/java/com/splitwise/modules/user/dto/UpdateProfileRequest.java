package com.splitwise.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @NotBlank(message = "Full name cannot be blank")
    private String fullName;
    private String phoneNumber;
    private String avatarUrl;
    private String defaultCurrency;
}

