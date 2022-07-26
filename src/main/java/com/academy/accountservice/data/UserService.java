package com.academy.accountservice.data;

import com.academy.accountservice.data.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    public static final int MAX_FAILED_ATTEMPTS = 5;
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(s.toLowerCase(Locale.ROOT));

        if (user.isPresent()){
            return new UserDetailsImpl(user.get());
        } else {
            throw new UsernameNotFoundException(String.format("Username[%s] not found"));
        }
    }

    public void increaseFailedAttempts(User user) {
        user.setFailedAttempt(user.getFailedAttempt() + 1);

        userRepository.save(user);
    }

    public void resetFailedAttempts(User user) {
        user.setFailedAttempt(0);

        userRepository.save(user);
    }

    public void lock(User user) {
        user.setAccountNonLocked(false);

        userRepository.save(user);
    }

    public void unlock(User user) {
        user.setAccountNonLocked(true);

        userRepository.save(user);
    }
}

