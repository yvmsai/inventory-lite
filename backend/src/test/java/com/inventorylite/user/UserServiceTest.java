package com.inventorylite.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.inventorylite.common.error.BusinessRuleException;
import com.inventorylite.common.error.ConflictException;
import com.inventorylite.common.error.NotFoundException;
import com.inventorylite.user.dto.CreateUserRequest;
import com.inventorylite.user.dto.UpdateUserRequest;
import com.inventorylite.user.dto.UserResponse;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserServiceTest {

    private final AppUserRepository repository = mock(AppUserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final UserService service = new UserService(repository, passwordEncoder, new UserMapper());

    @Test
    void create_normalizesEmailAndHashesPassword() {
        when(passwordEncoder.encode("Passw0rd!")).thenReturn("hash");
        when(repository.save(any(AppUser.class))).thenAnswer(i -> i.getArgument(0));

        UserResponse created = service.create(new CreateUserRequest(" Bob@X.com ", " Bob ", "Passw0rd!", Role.STAFF));

        ArgumentCaptor<AppUser> saved = ArgumentCaptor.forClass(AppUser.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo("bob@x.com");
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("hash");
        assertThat(created.fullName()).isEqualTo("Bob");
        assertThat(created.active()).isTrue();
    }

    @Test
    void create_duplicateEmailInAnyCase_throwsConflict() {
        when(repository.existsByEmail("bob@x.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateUserRequest("BOB@x.com", "Bob", "Passw0rd!", Role.STAFF)))
                .isInstanceOf(ConflictException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void deactivate_self_throwsBusinessRule() {
        assertThatThrownBy(() -> service.deactivate(7L, 7L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("You cannot deactivate yourself");
    }

    @Test
    void deactivate_otherUser_setsInactive() {
        AppUser user = new AppUser("a@x.com", "A", "h", Role.STAFF);
        when(repository.findById(5L)).thenReturn(Optional.of(user));
        when(repository.saveAndFlush(user)).thenReturn(user);

        assertThat(service.deactivate(5L, 1L).active()).isFalse();
    }

    @Test
    void activate_reactivatesUser() {
        AppUser user = new AppUser("a@x.com", "A", "h", Role.STAFF);
        user.deactivate();
        when(repository.findById(5L)).thenReturn(Optional.of(user));
        when(repository.saveAndFlush(user)).thenReturn(user);

        assertThat(service.activate(5L).active()).isTrue();
    }

    @Test
    void update_ownRole_throwsBusinessRule() {
        when(repository.findById(1L)).thenReturn(Optional.of(new AppUser("a@x.com", "A", "h", Role.ADMIN)));

        assertThatThrownBy(() -> service.update(1L, new UpdateUserRequest(null, Role.STAFF), 1L))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void update_otherUser_changesRoleAndName() {
        AppUser user = new AppUser("a@x.com", "A", "h", Role.STAFF);
        when(repository.findById(5L)).thenReturn(Optional.of(user));
        when(repository.saveAndFlush(user)).thenReturn(user);

        UserResponse updated = service.update(5L, new UpdateUserRequest(" New Name ", Role.ADMIN), 1L);

        assertThat(updated.role()).isEqualTo(Role.ADMIN);
        assertThat(updated.fullName()).isEqualTo("New Name");
    }

    @Test
    void update_ownNameOnly_isAllowed() {
        AppUser user = new AppUser("a@x.com", "A", "h", Role.ADMIN);
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.saveAndFlush(user)).thenReturn(user);

        assertThat(service.update(1L, new UpdateUserRequest("Me", Role.ADMIN), 1L).fullName()).isEqualTo("Me");
    }

    @Test
    void get_unknownId_throwsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(99L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void bootstrapAdmin_adminExists_doesNothing() {
        when(repository.existsByRole(Role.ADMIN)).thenReturn(true);

        service.bootstrapAdmin("root@x.com", "Passw0rd!");

        verify(repository, never()).save(any());
    }

    @Test
    void bootstrapAdmin_noAdmin_createsAdmin() {
        when(passwordEncoder.encode("Passw0rd!")).thenReturn("hash");
        when(repository.save(any(AppUser.class))).thenAnswer(i -> i.getArgument(0));

        service.bootstrapAdmin("Root@X.com", "Passw0rd!");

        ArgumentCaptor<AppUser> saved = ArgumentCaptor.forClass(AppUser.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getRole()).isEqualTo(Role.ADMIN);
        assertThat(saved.getValue().getEmail()).isEqualTo("root@x.com");
    }
}
