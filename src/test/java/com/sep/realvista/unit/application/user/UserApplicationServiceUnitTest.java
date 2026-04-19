package com.sep.realvista.unit.application.user;

import com.sep.realvista.application.billing.service.BillingApplicationService;
import com.sep.realvista.application.service.EmailService;
import com.sep.realvista.application.service.OtpService;
import com.sep.realvista.application.user.dto.CreateUserRequest;
import com.sep.realvista.application.user.mapper.UserMapper;
import com.sep.realvista.application.user.service.UserApplicationService;
import com.sep.realvista.domain.agent.repository.AgentProfileRepository;
import com.sep.realvista.domain.profile.repository.CustomerProfileRepository;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserDomainService;
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.domain.user.preference.SettingPreferenceRepository;
import com.sep.realvista.domain.user.role.Role;
import com.sep.realvista.domain.user.role.RoleCode;
import com.sep.realvista.domain.user.role.RoleRepository;
import com.sep.realvista.domain.user.role.UserRoleRepository;
import com.sep.realvista.infrastructure.security.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserApplicationServiceUnitTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserDomainService userDomainService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordService passwordService;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private SettingPreferenceRepository settingPreferenceRepository;
    @Mock
    private AgentProfileRepository agentProfileRepository;
    @Mock
    private CustomerProfileRepository customerProfileRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private OtpService otpService;
    @Mock
    private BillingApplicationService billingApplicationService;

    @InjectMocks
    private UserApplicationService userApplicationService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void createUser_createsAgentSuccessfully() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("agent@test.com");
        request.setPassword("Password123");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPhoneNumber("+1234567890");
        request.setRole("AGENT");

        User user = User.builder().build();
        Role role = Role.builder().roleCode(RoleCode.AGENT).build();

        doNothing().when(userDomainService).validateUniqueEmail(anyString());
        when(passwordService.encode(anyString())).thenReturn("hashed_pass");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(roleRepository.findByRoleCode(RoleCode.AGENT)).thenReturn(Optional.of(role));

        // Act
        userApplicationService.createUser(request);

        // Assert
        verify(settingPreferenceRepository, times(1)).save(any());
        verify(agentProfileRepository, times(1)).save(any());
        verify(customerProfileRepository, never()).save(any());
        verify(billingApplicationService, times(1)).assignDefaultAiPackage(any());
    }

    @Test
    void createUser_createsCustomerSuccessfully() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("customer@test.com");
        request.setPassword("Password123");
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setPhoneNumber("+1234567890");
        request.setRole("CUSTOMER");

        User user = User.builder().build();
        Role role = Role.builder().roleCode(RoleCode.BUYER).build();
        Role tenantRole = Role.builder().roleCode(RoleCode.TENANT).build();

        doNothing().when(userDomainService).validateUniqueEmail(anyString());
        when(passwordService.encode(anyString())).thenReturn("hashed_pass");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(roleRepository.findByRoleCode(RoleCode.BUYER)).thenReturn(Optional.of(role));
        when(roleRepository.findByRoleCode(RoleCode.TENANT)).thenReturn(Optional.of(tenantRole));

        // Act
        userApplicationService.createUser(request);

        // Assert
        verify(settingPreferenceRepository, times(1)).save(any());
        verify(customerProfileRepository, times(1)).save(any());
        verify(agentProfileRepository, never()).save(any());
        verify(billingApplicationService, times(1)).assignDefaultAiPackage(any());
    }
}
