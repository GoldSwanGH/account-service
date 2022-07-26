package com.academy.accountservice.data;

import com.academy.accountservice.data.entities.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserDetailsImpl implements UserDetails {

    private final String username;
    private final String password;
    private final boolean accountNonLocked;
    private final List<GrantedAuthority> rolesAndAuthorities;

    public UserDetailsImpl(User user) {

        username = user.getUsername();
        password = user.getPassword();
        accountNonLocked = user.isAccountNonLocked();
        rolesAndAuthorities = new ArrayList<>();
        user.getRoles().forEach(role -> {
            rolesAndAuthorities.add(new SimpleGrantedAuthority(role.getRole().name()));
        });
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return rolesAndAuthorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    // 4 remaining methods that just return true
    @Override
    public boolean isAccountNonExpired() {
        return accountNonLocked;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

