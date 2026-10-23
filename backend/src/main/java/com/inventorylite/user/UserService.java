package com.inventorylite.user;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import com.inventorylite.common.error.BusinessRuleException;
import com.inventorylite.common.error.ConflictException;
import com.inventorylite.common.error.NotFoundException;
import com.inventorylite.user.dto.CreateUserRequest;
import com.inventorylite.user.dto.UpdateUserRequest;
import com.inventorylite.user.dto.UserResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;

    public UserService(AppUserRepository repository, PasswordEncoder passwordEncoder, UserMapper mapper) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
    }

    public Page<UserResponse> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    public UserResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    public Optional<AppUser> findByEmail(String email) {
        return repository.findByEmail(normalize(email));
    }

    public AppUser getReference(Long id) {
        return repository.getReferenceById(id);
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        AppUser saved = createUser(request.email(), request.fullName(), request.password(), request.role());
        return mapper.toResponse(saved);
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request, Long currentUserId) {
        AppUser user = find(id);
        if (request.fullName() != null) {
            user.rename(request.fullName().trim());
        }
        if (request.role() != null && request.role() != user.getRole()) {
            if (Objects.equals(id, currentUserId)) {
                throw new BusinessRuleException("You cannot change your own role");
            }
            user.changeRole(request.role());
            log.info("User role changed id={} role={}", id, request.role());
        }
        return mapper.toResponse(repository.saveAndFlush(user));
    }

    @Transactional
    public UserResponse deactivate(Long id, Long currentUserId) {
        if (Objects.equals(id, currentUserId)) {
            throw new BusinessRuleException("You cannot deactivate yourself");
        }
        AppUser user = find(id);
        user.deactivate();
        log.info("User deactivated id={}", id);
        return mapper.toResponse(repository.saveAndFlush(user));
    }

    @Transactional
    public UserResponse activate(Long id) {
        AppUser user = find(id);
        user.activate();
        log.info("User activated id={}", id);
        return mapper.toResponse(repository.saveAndFlush(user));
    }

    @Transactional
    public void bootstrapAdmin(String email, String password) {
        if (repository.existsByRole(Role.ADMIN)) {
            return;
        }
        createUser(email, "Administrator", password, Role.ADMIN);
        log.info("Bootstrap ADMIN created");
    }

    private AppUser createUser(String email, String fullName, String password, Role role) {
        String normalized = normalize(email);
        if (repository.existsByEmail(normalized)) {
            throw new ConflictException("A user with email '%s' already exists".formatted(normalized));
        }
        AppUser saved = repository.save(
                new AppUser(normalized, fullName.trim(), passwordEncoder.encode(password), role));
        log.info("User created id={} role={}", saved.getId(), role);
        return saved;
    }

    private AppUser find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("User", id));
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
