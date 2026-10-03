package com.vestra.auth.service.service.auth;

import com.vestra.auth.service.dto.auth.*;
import com.vestra.auth.service.entity.User;
import com.vestra.auth.service.repository.UserRepository;
import com.vestra.auth.service.service.OutboxEventService;
import com.vestra.auth.service.service.token.TokenService;
import com.vestra.auth.service.service.user.UserService;
import com.vestra.common.web.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder encoder;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordResetService passwordResetService;
    private final OutboxEventService outboxEventService;
    private final LoginAttemptService loginAttemptService;
    private final UserService userService;

    @Transactional
    public AuthResponse register(CreateUserRequest request){
        String hashedPassword = encoder.encode(request.password());
        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(hashedPassword)
                .build();
        User savedUser = this.userRepository.save(user);
        String fullName = user.getFirstName() + " " + user.getLastName();
        outboxEventService.registeredEvent(savedUser.getId().toString(), fullName,savedUser.getEmail());
        return this.login(new LoginRequest(request.email(),request.password()));
    }

    public AuthResponse refresh(String oldRefreshToken) {
        if (oldRefreshToken==null){
            throw ApiException.unAuthorized("Kimlik Doğrulama Hatası,","Refresh Token eksik veya geçersiz.");
        }
        String userId = tokenService.validateAndGetUserId(oldRefreshToken);
        User user = this.userService.findById(UUID.fromString(userId));
        return tokenService.refresh(oldRefreshToken,user);
    }

    public AuthResponse login(LoginRequest request){

        if (loginAttemptService.isLocked(request.email())){
            throw ApiException.tooManyRequest(
                    "Hesap kilitlendi",
                    "Çok fazla başarısız deneme yapıldı lütfen biraz bekleyin."
            );
        }

        User user = this.userRepository.findByEmail(request.email())
                .orElseThrow(() -> ApiException.badRequest(null,"E-posta adresi veya şifre hatalı"));

        if (!encoder.matches(request.password(),user.getPassword())){
            loginAttemptService.recordFailedAttempt(user.getEmail());
            throw ApiException.badRequest(null,"E-posta adresi veya şifre hatalı");
        }
        loginAttemptService.resetAttempts(user.getEmail());
        return tokenService.create(user);

    }


    public void logout(String refreshToken){
        if (refreshToken==null){return;}
        this.tokenService.logout(refreshToken);
    }

    public void logoutAll(String userId){
        this.tokenService.logoutAll(userId);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request){

        this.userRepository.findByEmail(request.email()).ifPresent(user -> {
            String token = passwordResetService.createResetToken(user.getId().toString());
            outboxEventService.forgotPasswordEvent(user.getId().toString(),token,user.getEmail());
        });
    }

    public void resetPassword(ResetPasswordRequest request){
        String userId = passwordResetService.consumeToken(request.token());
        if (userId == null){
            throw ApiException.badRequest("Geçersiz token","Sıfırlama linki geçersiz veya süresi dolmuş");
        }
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> ApiException.badRequest(null,"Kullanıcı bulunamadı"));

        user.setPassword(encoder.encode(request.newPassword()));
        userRepository.save(user);
        this.tokenService.logoutAll(user.getId().toString());

    }




}
