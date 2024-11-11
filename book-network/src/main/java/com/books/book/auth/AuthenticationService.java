package com.books.book.auth;

import com.books.book.email.EmailService;
import com.books.book.email.EmailTemplateName;
import com.books.book.role.RoleRepository;
import com.books.book.security.JwtService;
import com.books.book.user.Token;
import com.books.book.user.TokenRepository;
import com.books.book.user.User;
import com.books.book.user.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    /*
     * All private properties are being injected by IoC
     */
    private final RoleRepository roleRepository;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

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
        String subject = "Account Activation";
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
        tokenRepository.save(token);
        return token.getToken();
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

    public AuthenticationResponse authenticate(@Valid AuthenticationRequest request) {
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (AuthenticationException e) {
            throw new RuntimeException("ErrorMessage: " + e.getMessage());
        }

        if (!auth.isAuthenticated()) {
            throw new RuntimeException("Authentication failed for user: " + request.getUsername());
        }

        User user = (User) auth.getPrincipal();
        Map<String, Object> claims = new HashMap<>();
        claims.put("fullname", user.fullName());
        String token = jwtService.generateToken(claims, user);
        return AuthenticationResponse.builder().token(token).build();
    }

    @Transactional
    public void activateAccount (String token) {
        try {
            Token userToken = tokenRepository.findByToken(token)
                    .orElseThrow(() -> new RuntimeException("Can't find requested token in Database"));

            if(userToken.getExpiresAt().isBefore(LocalDateTime.now())){
                sendValidationEmail(userToken.getUser());
                return;
            }


            User user = userRepository.findByEmail(userToken.getUser().getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userToken.getUser().getEmail()));
            user.setEnabled(true);
            userRepository.save(user);
            userToken.setValidatedAt(LocalDateTime.now());
            tokenRepository.save(userToken);
        } catch (Exception e) {
            // Handle the exception, log it, or rethrow it with a custom message
            throw new RuntimeException("Failed to activate account: " + e.getMessage(), e);
        }
    }
}
