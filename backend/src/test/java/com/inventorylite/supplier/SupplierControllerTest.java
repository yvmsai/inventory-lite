package com.inventorylite.supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.inventorylite.support.WebSliceTest;
import com.inventorylite.support.WebSliceTest.Tokens;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SupplierController.class)
@WebSliceTest
class SupplierControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private SupplierService service;

    @Test
    void list_withoutToken_returns401() throws Exception {
        mvc.perform(get("/api/v1/suppliers")).andExpect(status().isUnauthorized());
    }

    @Test
    void list_asStaff_returns200() throws Exception {
        when(service.list(eq("ac"), any())).thenReturn(new PageImpl<>(List.of()));

        mvc.perform(get("/api/v1/suppliers?q=ac").with(Tokens.staff()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void create_asStaff_returns403() throws Exception {
        mvc.perform(post("/api/v1/suppliers").with(Tokens.staff()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Acme\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_asStaff_returns403() throws Exception {
        mvc.perform(delete("/api/v1/suppliers/1").with(Tokens.staff())).andExpect(status().isForbidden());
    }

    @Test
    void create_invalidBody_returns400WithFieldErrors() throws Exception {
        mvc.perform(post("/api/v1/suppliers").with(Tokens.admin()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"nope\",\"phone\":\"%s\"}".formatted("1".repeat(31))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", Matchers.hasItems("name", "email", "phone")));
    }
}
