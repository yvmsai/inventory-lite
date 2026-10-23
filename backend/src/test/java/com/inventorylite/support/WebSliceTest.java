package com.inventorylite.support;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.inventorylite.security.ProblemAccessDeniedHandler;
import com.inventorylite.security.ProblemAuthenticationEntryPoint;
import com.inventorylite.security.SecurityConfig;

import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.ActiveProfiles;

/** Imports the real security configuration into a {@code @WebMvcTest} slice. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ActiveProfiles("test")
@Import({SecurityConfig.class, ProblemAuthenticationEntryPoint.class, ProblemAccessDeniedHandler.class})
public @interface WebSliceTest {

    final class Tokens {

        private Tokens() {
        }

        public static JwtRequestPostProcessor admin() {
            return as("ADMIN");
        }

        public static JwtRequestPostProcessor staff() {
            return as("STAFF");
        }

        private static JwtRequestPostProcessor as(String role) {
            return jwt().jwt(j -> j.subject("1")).authorities(new SimpleGrantedAuthority("ROLE_" + role));
        }
    }
}
