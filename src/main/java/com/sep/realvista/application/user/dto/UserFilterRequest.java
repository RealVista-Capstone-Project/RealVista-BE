package com.sep.realvista.application.user.dto;

import com.sep.realvista.domain.user.UserStatus;
import com.sep.realvista.domain.user.role.RoleCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for filtering users.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFilterRequest {
    private String search;
    private UserStatus status;
    private RoleCode role;
}
