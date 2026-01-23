package com.sep.realvista.application.user.mapper;

import com.sep.realvista.application.user.dto.CreateUserRequest;
import com.sep.realvista.application.user.dto.UserResponse;
import com.sep.realvista.domain.common.value.Email;
import com.sep.realvista.domain.user.role.UserRole;
import com.sep.realvista.domain.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for User entity and DTOs.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "email", source = "email.value")
    @Mapping(target = "fullName", source = ".", qualifiedByName = "getFullName")
    @Mapping(target = "roles", source = "userRoles", qualifiedByName = "userRolesToStrings")
    UserResponse toResponse(User user);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "email", source = "email", qualifiedByName = "stringToEmail")
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "passwordHash", source = "password")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "emailVerifiedAt", ignore = true)
    @Mapping(target = "phoneVerifiedAt", ignore = true)
    @Mapping(target = "businessName", ignore = true)
    User toEntity(CreateUserRequest request);

    @Named("stringToEmail")
    default Email stringToEmail(String email) {
        return email != null ? Email.of(email) : null;
    }

    @Named("getFullName")
    default String getFullName(User user) {
        return user.getFullName();
    }

    @Named("userRolesToStrings")
    default Set<String> userRolesToStrings(Set<UserRole> userRoles) {
        if (userRoles == null) {
            return Set.of();
        }
        return userRoles.stream()
                .filter(ur -> ur.getRole() != null)
                .map(ur -> ur.getRole().getRoleCode().name())
                .collect(Collectors.toSet());
    }
}

