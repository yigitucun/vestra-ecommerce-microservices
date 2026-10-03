package com.vestra.auth.service.service.oauth2;

import com.vestra.auth.service.entity.User;
import com.vestra.auth.service.repository.UserRepository;
import com.vestra.auth.service.service.OutboxEventService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class Oauth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final OutboxEventService outboxEventService;

    @Override
    @Transactional
    public OAuth2User loadUser(@NonNull OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            throw new OAuth2AuthenticationException("Google hesabından email bilgisi alınamadı");
        }
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder()


                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .build();
            User savedUser = this.userRepository.save(newUser);
            String fullName = savedUser.getFirstName() + " " + savedUser.getLastName();

            outboxEventService.registeredEvent(savedUser.getId().toString(), fullName,savedUser.getEmail());

            return savedUser;
        });
        return new CustomOAuth2User(user,oAuth2User);
    }
}
