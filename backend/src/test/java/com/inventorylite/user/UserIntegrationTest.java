package com.inventorylite.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.inventorylite.support.IntegrationTest;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class UserIntegrationTest extends IntegrationTest {

    @Test
    void create_asAdmin_returns201AndUserCanLogIn() throws Exception {
        String email = unique("new") + "@test.local";

        mvc.perform(post("/api/v1/users").header("Authorization", bearer(Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(email.toUpperCase())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", Matchers.containsString("/api/v1/users/")))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.active").value(true));

        bearerFor(email);
    }

    @Test
    void create_duplicateEmailDifferentCase_returns409() throws Exception {
        String email = createUser(Role.STAFF);

        mvc.perform(post("/api/v1/users").header("Authorization", bearer(Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content(body(email.toUpperCase())))
                .andExpect(status().isConflict());
    }

    @Test
    void create_asStaff_returns403() throws Exception {
        mvc.perform(post("/api/v1/users").header("Authorization", bearer(Role.STAFF))
                        .contentType(MediaType.APPLICATION_JSON).content(body(unique("x") + "@test.local")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deactivate_self_returns422() throws Exception {
        String admin = createUser(Role.ADMIN);

        mvc.perform(post("/api/v1/users/{id}/deactivate", userId(admin)).header("Authorization", bearerFor(admin)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("You cannot deactivate yourself"));
    }

    @Test
    void deactivateThenActivate_otherUser_togglesLoginAbility() throws Exception {
        String adminToken = bearer(Role.ADMIN);
        String staff = createUser(Role.STAFF);
        Long staffId = userId(staff);

        mvc.perform(post("/api/v1/users/{id}/deactivate", staffId).header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(staff, PASSWORD)))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/api/v1/users/{id}/activate", staffId).header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
        bearerFor(staff);
    }

    @Test
    void update_changeOwnRole_returns422_otherRole_returns200() throws Exception {
        String admin = createUser(Role.ADMIN);
        String token = bearerFor(admin);
        String staff = createUser(Role.STAFF);

        mvc.perform(patch("/api/v1/users/{id}", userId(admin)).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"STAFF\"}"))
                .andExpect(status().isUnprocessableContent());
        mvc.perform(patch("/api/v1/users/{id}", userId(staff)).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void list_asAdmin_returnsPageShape() throws Exception {
        mvc.perform(get("/api/v1/users?size=5").header("Authorization", bearer(Role.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.content[0].passwordHash").doesNotExist());
    }

    @Test
    void get_unknownUser_returns404() throws Exception {
        mvc.perform(patch("/api/v1/users/{id}", 999999).header("Authorization", bearer(Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"fullName\":\"X\"}"))
                .andExpect(status().isNotFound());
    }

    private static String body(String email) {
        return "{\"email\":\"%s\",\"fullName\":\"New User\",\"password\":\"Passw0rd!\",\"role\":\"STAFF\"}"
                .formatted(email);
    }
}
