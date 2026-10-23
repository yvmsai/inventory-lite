package com.inventorylite.user;

import java.net.URI;

import com.inventorylite.common.web.PageResponse;
import com.inventorylite.security.CurrentUser;
import com.inventorylite.user.dto.CreateUserRequest;
import com.inventorylite.user.dto.UpdateUserRequest;
import com.inventorylite.user.dto.UserResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Users")
class UserController {

    private final UserService service;

    UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    PageResponse<UserResponse> list(@ParameterObject @PageableDefault(size = 20, sort = "email",
            direction = Sort.Direction.ASC) Pageable pageable) {
        return PageResponse.from(service.list(pageable));
    }

    @PostMapping
    ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request, UriComponentsBuilder uri) {
        UserResponse created = service.create(request);
        URI location = uri.path("/api/v1/users/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PatchMapping("/{id}")
    UserResponse update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request,
                        @AuthenticationPrincipal Jwt jwt) {
        return service.update(id, request, CurrentUser.id(jwt));
    }

    @PostMapping("/{id}/deactivate")
    UserResponse deactivate(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.deactivate(id, CurrentUser.id(jwt));
    }

    @PostMapping("/{id}/activate")
    UserResponse activate(@PathVariable Long id) {
        return service.activate(id);
    }
}
