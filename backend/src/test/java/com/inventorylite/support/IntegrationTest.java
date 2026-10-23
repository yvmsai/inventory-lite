package com.inventorylite.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.inventorylite.TestcontainersConfiguration;
import com.inventorylite.user.AppUserRepository;
import com.inventorylite.user.Role;
import com.inventorylite.user.UserService;
import com.inventorylite.user.dto.CreateUserRequest;
import com.jayway.jsonpath.JsonPath;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base for end-to-end tests against a real PostgreSQL (Testcontainers). The Spring context, and so the
 * container, is shared by all subclasses, so tests create uniquely named data instead of assuming an empty DB.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class IntegrationTest {

    protected static final String PASSWORD = "Passw0rd!";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected UserService userService;

    @Autowired
    protected AppUserRepository userRepository;

    protected static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /** Creates a fresh user with the given role and returns "Bearer <token>". */
    protected String bearer(Role role) throws Exception {
        return bearerFor(createUser(role));
    }

    protected String createUser(Role role) {
        String email = unique(role.name().toLowerCase()) + "@test.local";
        userService.create(new CreateUserRequest(email, "Test " + role, PASSWORD, role));
        return email;
    }

    protected String bearerFor(String email) throws Exception {
        String body = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(body, "$.accessToken");
    }

    protected Long userId(String email) {
        return userRepository.findByEmail(email).orElseThrow().getId();
    }
}
