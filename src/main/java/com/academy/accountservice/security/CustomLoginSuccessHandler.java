package com.academy.accountservice.security;

import com.academy.accountservice.data.UserDetailsImpl;
import com.academy.accountservice.data.UserRepository;
import com.academy.accountservice.data.UserService;
import com.academy.accountservice.data.entities.User;
import com.academy.accountservice.exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(UserNotFoundException::new);

        if (user.getFailedAttempt() > 0) {
            userService.resetFailedAttempts(user);
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }

}
