package com.inventorylite.user;

import com.inventorylite.user.dto.UserResponse;

import org.springframework.stereotype.Component;

@Component
class UserMapper {

    UserResponse toResponse(AppUser user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole(),
                user.isActive(), user.getCreatedAt(), user.getUpdatedAt());
    }
}
