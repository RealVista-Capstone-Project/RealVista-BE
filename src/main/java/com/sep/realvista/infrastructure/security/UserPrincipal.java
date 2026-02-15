package com.sep.realvista.infrastructure.security;

import com.sep.realvista.domain.user.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class UserPrincipal implements UserDetails {
    private final UUID userId;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean active;

    public UserPrincipal(User user) {
        this.userId = user.getUserId();
        this.email = user.getEmail().getValue();
        this.password = user.getPasswordHash();
        this.active = user.isActive();
        
        this.authorities = user.getUserRoles().isEmpty() 
            ? List.of(new SimpleGrantedAuthority("ROLE_BUYER")) // Default
            : user.getUserRoles().stream()
                .filter(ur -> ur.getRole() != null)
                .map(ur -> new SimpleGrantedAuthority("ROLE_" + ur.getRole().getRoleCode().name()))
                .collect(Collectors.toList());
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
        return email;
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
