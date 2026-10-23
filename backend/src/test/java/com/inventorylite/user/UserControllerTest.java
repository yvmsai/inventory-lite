package com.inventorylite.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.inventorylite.support.WebSliceTest;
import com.inventorylite.support.WebSliceTest.Tokens;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@WebSliceTest
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UserService service;

    @Test
    void list_withoutToken_returns401() throws Exception {
        mvc.perform(get("/api/v1/users")).andExpect(status().isUnauthorized());
    }

    @Test
    void list_asStaff_returns403() throws Exception {
        mvc.perform(get("/api/v1/users").with(Tokens.staff())).andExpect(status().isForbidden());
    }

    @Test
    void create_asStaff_returns403() throws Exception {
        mvc.perform(post("/api/v1/users").with(Tokens.staff()).contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_invalidBody_returns400WithFieldErrors() throws Exception {
        mvc.perform(post("/api/v1/users").with(Tokens.admin()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\",\"fullName\":\"\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field",
                        Matchers.hasItems("email", "fullName", "password", "role")));
    }

    private static String validBody() {
        return "{\"email\":\"a@x.com\",\"fullName\":\"A\",\"password\":\"Passw0rd!\",\"role\":\"STAFF\"}";
    }
}
