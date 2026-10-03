package com.vestra.auth.service.service.user;

import com.vestra.auth.service.dto.user.UpdateUserRequest;
import com.vestra.auth.service.entity.User;
import com.vestra.auth.service.projection.user.ListAllUser;
import com.vestra.auth.service.repository.UserRepository;
import com.vestra.common.enums.Role;
import com.vestra.common.web.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Cacheable(value = "users", key = "#id")
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ApiException.unAuthorized("Yetkisiz erişim", "Oturum geçersiz, lütfen tekrar giriş yapın."));
    }

    public PagedModel<ListAllUser> findAllUser(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<ListAllUser> users;
        if (search != null && !search.trim().isEmpty()) {
            users = userRepository.searchUsers(search.trim(), pageable);
        } else {
            users = userRepository.findAllBy(pageable);
        }

        return new PagedModel<>(users);
    }

    @CacheEvict(value = "users", key = "#id")
    @Transactional
    public void updateUser(UUID id, UpdateUserRequest request, UUID currentUserId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Kullanıcı Bulunamadı", "Güncellenmek istenen kullanıcı bulunamadı."));

        if (currentUserId != null && currentUserId.equals(id)) {
            if (request.role() != Role.ADMIN) {
                throw ApiException.badRequest("Geçersiz İşlem", "Kendi hesabınızın yönetici (ADMIN) rolünü kaldıramazsınız.");
            }
            if (Boolean.FALSE.equals(request.isActive())) {
                throw ApiException.badRequest("Geçersiz İşlem", "Kendi hesabınızı pasife alamazsınız.");
            }
        }

        String newEmail = request.email().trim().toLowerCase();
        if (!user.getEmail().equalsIgnoreCase(newEmail) && userRepository.existsByEmail(newEmail)) {
            throw ApiException.conflict("E-posta Kullanımda", "Bu e-posta adresi ('" + newEmail + "') başka bir kullanıcı tarafından kullanılmaktadır.");
        }

        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(newEmail);
        user.setRole(request.role());
        if (request.isActive() != null) {
            user.setActive(request.isActive());
        }

        if (request.password() != null && !request.password().isBlank()) {
            if (request.password().trim().length() < 6) {
                throw ApiException.badRequest("Geçersiz Şifre", "Şifre en az 6 karakter olmalıdır.");
            }
            user.setPassword(passwordEncoder.encode(request.password().trim()));
        }

        userRepository.save(user);
    }

    @CacheEvict(value = "users", key = "#id")
    @Transactional
    public void deleteUser(UUID id, UUID currentUserId) {
        if (currentUserId != null && currentUserId.equals(id)) {
            throw ApiException.badRequest("Geçersiz İşlem", "Kendi hesabınızı silemezsiniz.");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Kullanıcı Bulunamadı", "Silinmek istenen kullanıcı bulunamadı."));
        userRepository.delete(user);
    }
}
