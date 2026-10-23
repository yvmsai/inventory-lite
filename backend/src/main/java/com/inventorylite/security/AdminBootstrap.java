package com.inventorylite.security;

import com.inventorylite.user.UserService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** Creates the first ADMIN from ADMIN_EMAIL / ADMIN_PASSWORD when no admin exists (used in prod). */
@Component
class AdminBootstrap implements ApplicationRunner {

    private final UserService userService;
    private final String email;
    private final String password;

    AdminBootstrap(UserService userService,
                   @Value("${app.bootstrap.admin-email:}") String email,
                   @Value("${app.bootstrap.admin-password:}") String password) {
        this.userService = userService;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (StringUtils.hasText(email) && StringUtils.hasText(password)) {
            userService.bootstrapAdmin(email, password);
        }
    }
}
