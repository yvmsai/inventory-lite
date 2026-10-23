package com.inventorylite.security;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.inventorylite.support.IntegrationTest;
import com.inventorylite.user.Role;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class AuthIntegrationTest extends IntegrationTest {

    @Test
    void login_validCredentials_returnsTokenThatAuthenticatesMe() throws Exception {
        String email = createUser(Role.STAFF);

        mvc.perform(get("/api/v1/auth/me").header("Authorization", bearerFor(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("STAFF"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void login_emailIsCaseInsensitive() throws Exception {
        String email = createUser(Role.STAFF);

        login(email.toUpperCase(), PASSWORD).andExpect(status().isOk())
                .andExpect(jsonPath("$.expiresIn").value(1800));
    }

    @Test
    void login_wrongPasswordAndUnknownEmail_returnSame401() throws Exception {
        String email = createUser(Role.STAFF);

        login(email, "wrong-password").andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid email or password"));
        login("nobody@test.local", PASSWORD).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid email or password"));
    }

    @Test
    void login_deactivatedUser_returns401() throws Exception {
        String email = createUser(Role.STAFF);
        userService.deactivate(userId(email), -1L);

        login(email, PASSWORD).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid email or password"));
    }

    @Test
    void login_invalidBody_returns400WithFieldErrors() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors[*].field", org.hamcrest.Matchers.hasItems("email", "password")));
    }

    @Test
    void me_withoutToken_returns401ProblemDetail() throws Exception {
        mvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", containsString("Bearer")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void me_withTamperedToken_returns401() throws Exception {
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void health_isPublic() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    private org.springframework.test.web.servlet.ResultActions login(String email, String password) throws Exception {
        return mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)));
    }
}
