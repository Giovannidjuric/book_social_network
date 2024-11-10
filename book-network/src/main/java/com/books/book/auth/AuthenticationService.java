package com.books.book.auth;

import com.books.book.email.EmailService;
import com.books.book.email.EmailTemplateName;
import com.books.book.role.RoleRepository;
import com.books.book.user.Token;
import com.books.book.user.TokenRepository;
import com.books.book.user.User;
import com.books.book.user.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final RoleRepository roleRepository;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final String subject = "Account Activation";
    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;


    public void register(RegistrationRequest request) throws MessagingException {
        var role = roleRepository.findByName("USER").orElseThrow(() -> new IllegalStateException("Role not found"));
        User user = User.builder()
                .firstName(request.getFirstname())
                .lastName(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(List.of(role))
                .accountLocked(false)
                .enabled(false)
                .build();

        userRepository.save(user);
        sendValidationEmail(user);

    }

    private void sendValidationEmail (User user) throws MessagingException {
        String token = generateAndSaveActivationToken(user);
        emailService.sendEmail(
                user.getEmail(),
                user.fullName(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl,
                token,
                subject
                );



    }

    private String generateAndSaveActivationToken(User user){
        String activationCode = generateActivationCode(6);
        var token = Token
                .builder()
                .token(activationCode)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();
        return tokenRepository.save(token).getToken();
    }

    private String generateActivationCode(int length){
        String characters = "0123456789";
        StringBuilder builder = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for(int i = 0;i < length;i++){
            int randomIndex = random.nextInt(characters.length());
            builder.append(randomIndex);
        }
        return builder.toString();
    }
}
