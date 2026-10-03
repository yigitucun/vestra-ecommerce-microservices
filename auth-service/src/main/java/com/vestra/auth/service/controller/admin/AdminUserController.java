package com.vestra.auth.service.controller.admin;

import com.vestra.auth.service.dto.user.UpdateUserRequest;
import com.vestra.auth.service.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String search
    ){
        return ResponseEntity.ok(userService.findAllUser(page, size, search));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID currentUserId = jwt != null && jwt.getSubject() != null ? UUID.fromString(jwt.getSubject()) : null;
        userService.updateUser(id, request, currentUserId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID currentUserId = jwt != null && jwt.getSubject() != null ? UUID.fromString(jwt.getSubject()) : null;
        userService.deleteUser(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
