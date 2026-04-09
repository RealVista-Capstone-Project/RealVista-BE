package com.sep.realvista.infrastructure.security;

import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/**
 * Custom UserDetailsService implementation for Spring Security.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailValue(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw new UsernameNotFoundException("Account has been deleted");
        }

        List<SimpleGrantedAuthority> authorities = user.getUserRoles().stream()
                .filter(ur -> ur.getRole() != null)
                .map(ur -> new SimpleGrantedAuthority("ROLE_" + ur.getRole().getRoleCode().name()))
                .collect(Collectors.toList());

        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_BUYER"));
        }

        return new SecurityUserDetails(
                user.getUserId(),
                user.getEmail().getValue(),
                user.getPasswordHash(),
                authorities,
                user.isActive()
        );
    }
}
