package com.academy.accountservice.security;

import com.academy.accountservice.data.UserRepository;
import com.academy.accountservice.data.UserService;
import com.academy.accountservice.data.entities.User;
import com.academy.accountservice.exceptions.UserNotFoundException;
import com.academy.accountservice.logging.SecurityEvent;
import com.academy.accountservice.logging.SecurityEventLogger;
import com.academy.accountservice.models.UserSignUpModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

@Component
public class CustomLoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private final SecurityEventLogger Logger = SecurityEventLogger.getInstance();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        ObjectMapper objectMapper = new ObjectMapper();
        UserSignUpModel body = objectMapper.readValue(requestBody, UserSignUpModel.class);

        User user = userRepository.findByUsername(body.getEmail()).orElseThrow(UserNotFoundException::new);

        Logger.log(SecurityEvent.LOGIN_FAILED, user.getUsername(), request.getRequestURI(), request.getRequestURI());

        if (user.isAccountNonLocked()) {

            if (user.getFailedAttempt() < UserService.MAX_FAILED_ATTEMPTS - 1) {
                userService.increaseFailedAttempts(user);
            } else {
                Logger.log(SecurityEvent.BRUTE_FORCE, user.getUsername(), request.getRequestURI(),
                        request.getRequestURI());

                userService.lock(user);

                Logger.log(SecurityEvent.LOCK_USER, user.getUsername(), "Lock user " + user.getUsername(),
                        request.getRequestURI());

                exception = new LockedException("Your account has been locked due to " +
                        UserService.MAX_FAILED_ATTEMPTS + " failed attempts.");
            }
        }

        super.onAuthenticationFailure(request, response, exception);
    }
}