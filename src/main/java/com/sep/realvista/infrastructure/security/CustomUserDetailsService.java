package com.sep.realvista.infrastructure.security;

import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom UserDetailsService implementation for Spring Security.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailValue(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        List<SimpleGrantedAuthority> authorities = user.getUserRoles().stream()
                .filter(ur -> ur.getRole() != null)
                .map(ur -> new SimpleGrantedAuthority("ROLE_" + ur.getRole().getRoleCode().name()))
                .collect(Collectors.toList());

        // Default to BUYER role if no roles assigned
        if (authorities.isEmpty()) {
            authorities = List.of(new SimpleGrantedAuthority("ROLE_BUYER"));
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail().getValue())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!user.isActive())
                .credentialsExpired(false)
                .disabled(!user.isActive())
                .build();
    }
}

