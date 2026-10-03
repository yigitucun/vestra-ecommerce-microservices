package com.vestra.auth.service.service.user;

import com.vestra.auth.service.entity.User;
import com.vestra.auth.service.projection.user.ListAllUser;
import com.vestra.auth.service.repository.UserRepository;
import com.vestra.common.web.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Cacheable(value = "users",key = "#id")
    public User findById(UUID id){
        return userRepository.findById(id)
                .orElseThrow(() -> ApiException.unAuthorized("Yetkisiz erişim","Oturum geçersiz, lütfen tekrar giriş yapın."));
    }


    public PagedModel<ListAllUser> findAllUser(int page, int size, String search){
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<ListAllUser> users;
        if (search != null && !search.trim().isEmpty()) {
            users = userRepository.searchUsers(search.trim(), pageable);
        } else {
            users = userRepository.findAllBy(pageable);
        }

        return new PagedModel<>(users);
    }




}
