package com.vestra.auth.service.service.token;

import com.vestra.auth.service.dto.auth.AuthResponse;
import com.vestra.auth.service.dto.user.UserInfo;
import com.vestra.auth.service.entity.User;
import com.vestra.auth.service.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenService refreshTokenService;
    private final AccessTokenService accessTokenService;
    private final UserMapper userMapper;

    public AuthResponse create(User user){
        String accessToken = accessTokenService.generateAccessToken(user);
        String refreshToken = refreshTokenService.create(user.getId().toString());
        UserInfo userInfo = this.userMapper.toDto(user);
        return new AuthResponse(accessToken,refreshToken, userInfo);
    }

    public void logout(String refreshToken){
        String userId = refreshTokenService.validateAndGetUserId(refreshToken);
        refreshTokenService.revoke(refreshToken,userId);
    }

    public void logoutAll(String userId){
        refreshTokenService.revokeAll(userId);
    }

    public String validateAndGetUserId(String oldRefreshToken){
        return this.refreshTokenService.validateAndGetUserId(oldRefreshToken);
    }

    public AuthResponse refresh(String oldRefreshToken,User user){
        String newAccessToken = accessTokenService.generateAccessToken(user);
        String newRefreshToken = refreshTokenService.rotate(oldRefreshToken);
        UserInfo userInfo = this.userMapper.toDto(user);
        return new AuthResponse(newAccessToken,newRefreshToken, userInfo);
    }


}
