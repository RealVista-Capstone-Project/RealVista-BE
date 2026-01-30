package com.sep.realvista.application.auth.mapper;

import com.sep.realvista.application.auth.dto.AuthenticationResponse;
import com.sep.realvista.domain.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Authentication-related DTOs.
 * <p>
 * Separates mapping logic from business logic following Single Responsibility Principle.
 * This mapper is responsible solely for transforming domain objects to authentication DTOs.
 */
@Mapper(componentModel = "spring")
public interface AuthenticationMapper {

    /**
     * Maps User entity and token to AuthenticationResponse.
     *
     * @param user  the authenticated user
     * @param token the generated authentication token
     * @return the authentication response DTO
     */
    @Mapping(target = "token", source = "token")
    @Mapping(target = "type", constant = "Bearer")
    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "email", source = "user.email.value")
    @Mapping(target = "roles", expression = "java(mapRoles(user))")
    AuthenticationResponse toAuthenticationResponse(User user, String token);

    /**
     * Maps User roles to a list of role codes.
     *
     * @param user the user entity
     * @return list of role codes
     */
    default java.util.List<String> mapRoles(User user) {
        return user.getUserRoles().stream()
                .filter(ur -> ur.getRole() != null)
                .map(ur -> ur.getRole().getRoleCode().name())
                .toList();
    }
}
