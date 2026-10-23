package com.inventorylite.supplier;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import com.inventorylite.product.ProductService;
import com.inventorylite.product.dto.ProductRequest;
import com.inventorylite.support.IntegrationTest;
import com.inventorylite.supplier.dto.SupplierRequest;
import com.inventorylite.supplier.dto.SupplierResponse;
import com.inventorylite.user.Role;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class SupplierIntegrationTest extends IntegrationTest {

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private ProductService productService;

    @Test
    void create_asAdmin_returns201() throws Exception {
        String name = unique("Acme");

        mvc.perform(post("/api/v1/suppliers").header("Authorization", bearer(Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"%s\",\"email\":\"sales@acme.test\",\"phone\":\"+1 555\"}".formatted(name)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.version").value(0));
    }

    @Test
    void create_duplicateNameDifferentCase_returns409() throws Exception {
        String name = unique("Dup");
        supplierService.create(new SupplierRequest(name, null, null, null));

        mvc.perform(post("/api/v1/suppliers").header("Authorization", bearer(Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"%s\"}".formatted(name.toUpperCase())))
                .andExpect(status().isConflict());
    }

    @Test
    void list_searchByName_isCaseInsensitiveContains() throws Exception {
        String marker = unique("Zebra");
        supplierService.create(new SupplierRequest("North " + marker + " Supplies", null, null, null));

        mvc.perform(get("/api/v1/suppliers").param("q", marker.toUpperCase()).header("Authorization", bearer(Role.STAFF)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name", Matchers.containsString(marker)));
    }

    @Test
    void update_withCurrentVersion_returns200_staleVersion_returns409() throws Exception {
        SupplierResponse supplier = supplierService.create(new SupplierRequest(unique("Upd"), null, null, null));
        String token = bearer(Role.ADMIN);
        String body = "{\"name\":\"%s\",\"version\":%d}";

        mvc.perform(put("/api/v1/suppliers/{id}", supplier.id()).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(body.formatted(supplier.name() + "x", 0)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(1));
        mvc.perform(put("/api/v1/suppliers/{id}", supplier.id()).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(body.formatted(supplier.name() + "y", 0)))
                .andExpect(status().isConflict());
    }

    @Test
    void delete_supplierWithProducts_returns409_withoutProducts_returns204() throws Exception {
        SupplierResponse used = supplierService.create(new SupplierRequest(unique("Used"), null, null, null));
        productService.create(new ProductRequest(unique("SKU").toUpperCase(), "Widget", BigDecimal.ONE, null, used.id(), null));
        SupplierResponse unused = supplierService.create(new SupplierRequest(unique("Unused"), null, null, null));
        String token = bearer(Role.ADMIN);

        mvc.perform(delete("/api/v1/suppliers/{id}", used.id()).header("Authorization", token))
                .andExpect(status().isConflict());
        mvc.perform(delete("/api/v1/suppliers/{id}", unused.id()).header("Authorization", token))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/suppliers/{id}", unused.id()).header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_asStaff_returns403() throws Exception {
        mvc.perform(post("/api/v1/suppliers").header("Authorization", bearer(Role.STAFF))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"%s\"}".formatted(unique("S"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void list_unknownSortProperty_returns400() throws Exception {
        mvc.perform(get("/api/v1/suppliers?sort=bogus").header("Authorization", bearer(Role.STAFF)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Unknown sort property 'bogus'"));
    }
}
