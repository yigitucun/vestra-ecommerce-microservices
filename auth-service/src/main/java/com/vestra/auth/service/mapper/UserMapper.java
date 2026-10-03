package com.vestra.auth.service.mapper;

import com.vestra.auth.service.dto.user.UserInfo;
import com.vestra.auth.service.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper{
    UserInfo toDto(User user);
}
