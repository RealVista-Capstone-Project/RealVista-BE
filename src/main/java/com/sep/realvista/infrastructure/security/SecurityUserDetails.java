package com.sep.realvista.infrastructure.security;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

@Builder
public class SecurityUserDetails implements UserDetails {

    @Getter
    private final UUID userId;
    private final String username; // Email
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean active;

    public SecurityUserDetails(UUID userId,
                               String email,
                               String password,
                               Collection<? extends GrantedAuthority> authorities,
                               boolean active) {
        this.userId = userId;
        this.username = email;
        this.password = password;
        this.authorities = authorities;
        this.active = active;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
