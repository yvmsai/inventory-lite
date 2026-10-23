package com.inventorylite.product;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.inventorylite.support.WebSliceTest;
import com.inventorylite.support.WebSliceTest.Tokens;

import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
@WebSliceTest
class ProductControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ProductService service;

    @Test
    void get_withoutToken_returns401() throws Exception {
        mvc.perform(get("/api/v1/products/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void create_asStaff_returns403() throws Exception {
        mvc.perform(post("/api/v1/products").with(Tokens.staff()).contentType(MediaType.APPLICATION_JSON)
                        .content(body("ABC-1", "\"1.00\"", "5")))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_asStaff_returns403() throws Exception {
        mvc.perform(put("/api/v1/products/1").with(Tokens.staff()).contentType(MediaType.APPLICATION_JSON)
                        .content(body("ABC-1", "\"1.00\"", "5")))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest(name = "sku={0} price={1} reorder={2} -> error on {3}")
    @CsvSource(delimiter = '|', value = {
            "abc-1   | \"1.00\"  | 5  | sku",
            "AB C    | \"1.00\"  | 5  | sku",
            "ABC_1   | \"1.00\"  | 5  | sku",
            "ABC-1   | \"-0.01\" | 5  | unitPrice",
            "ABC-1   | \"1.005\" | 5  | unitPrice",
            "ABC-1   | \"1.00\"  | -1 | reorderLevel"})
    void create_invalidField_returns400(String sku, String price, String reorder, String field) throws Exception {
        mvc.perform(post("/api/v1/products").with(Tokens.admin()).contentType(MediaType.APPLICATION_JSON)
                        .content(body(sku, price, reorder)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", Matchers.hasItem(field)));
    }

    @Test
    void create_skuLongerThan64_returns400() throws Exception {
        mvc.perform(post("/api/v1/products").with(Tokens.admin()).contentType(MediaType.APPLICATION_JSON)
                        .content(body("A".repeat(65), "\"1.00\"", "5")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", Matchers.hasItem("sku")));
    }

    @Test
    void create_malformedJson_returns400ProblemDetail() throws Exception {
        mvc.perform(post("/api/v1/products").with(Tokens.admin()).contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    private static String body(String sku, String price, String reorder) {
        return "{\"sku\":\"%s\",\"name\":\"Widget\",\"unitPrice\":%s,\"reorderLevel\":%s,\"supplierId\":1}"
                .formatted(sku, price, reorder);
    }
}
