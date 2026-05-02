package com.sep.realvista.unit.application.user;

import com.sep.realvista.application.billing.service.BillingApplicationService;
import com.sep.realvista.application.service.EmailService;
import com.sep.realvista.application.service.OtpService;
import com.sep.realvista.application.user.dto.CreateUserRequest;
import com.sep.realvista.application.user.mapper.UserMapper;
import com.sep.realvista.application.user.service.UserApplicationService;
import com.sep.realvista.domain.agent.PropertyAgent;
import com.sep.realvista.domain.agent.PropertyAgentRepository;
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
import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.PropertyStatus;
import com.sep.realvista.domain.property.repository.PropertyRepository;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.listing.appointment.Appointment;
import com.sep.realvista.domain.listing.appointment.AppointmentStatus;
import com.sep.realvista.domain.listing.repository.AppointmentRepository;
import com.sep.realvista.domain.billing.boost.ListingBoost;
import com.sep.realvista.domain.billing.boost.repository.ListingBoostRepository;
import com.sep.realvista.domain.engagement.Engagement;
import com.sep.realvista.domain.engagement.EngagementStatus;
import com.sep.realvista.domain.engagement.EngagementRepository;
import com.sep.realvista.domain.engagement.proposal.AgentProposal;
import com.sep.realvista.domain.engagement.proposal.AgentProposalRepository;
import com.sep.realvista.domain.billing.subscription.UserFeatureSubscription;
import com.sep.realvista.domain.billing.subscription.repository.UserFeatureSubscriptionRepository;
import com.sep.realvista.infrastructure.security.PasswordService;
import org.springframework.data.domain.PageImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Optional;
import java.util.List;
import java.util.UUID;
import java.util.Collections;
import java.util.Set;

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
    @Mock
    private ListingRepository listingRepository;
    @Mock
    private PropertyRepository propertyRepository;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private ListingBoostRepository listingBoostRepository;
    @Mock
    private EngagementRepository engagementRepository;
    @Mock
    private AgentProposalRepository agentProposalRepository;
    @Mock
    private UserFeatureSubscriptionRepository userFeatureSubscriptionRepository;
    @Mock
    private PropertyAgentRepository propertyAgentRepository;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private Cache listingCache;

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
        verify(billingApplicationService, times(1)).assignAllDefaultFreePackages(any());
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

    @Test
    void suspendUser_shouldCascadeCleanup() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = spy(User.builder().userId(userId).status(com.sep.realvista.domain.user.UserStatus.ACTIVE).build());
        
        UUID propertyId = UUID.randomUUID();
        Property property = spy(Property.builder().propertyId(propertyId).build());
        
        UUID listingId = UUID.randomUUID();
        Listing listing = spy(Listing.builder().listingId(listingId).status(com.sep.realvista.domain.listing.ListingStatus.PUBLISHED).build());
        
        Appointment appointment = spy(Appointment.builder().build());
        ListingBoost boost = spy(ListingBoost.builder().build());
        Engagement engagement = spy(Engagement.builder().status(EngagementStatus.SUBMITTED).build());
        AgentProposal proposal = spy(AgentProposal.builder()
                .status(com.sep.realvista.domain.engagement.proposal.AgentProposalStatus.ACTIVE).build());

        when(userDomainService.getUserOrThrow(userId)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        when(propertyRepository.findByOwnerId(userId)).thenReturn(List.of(property));
        when(listingRepository.findByUserIdOrPropertyOwnerId(userId)).thenReturn(List.of(listing));
        when(cacheManager.getCache("listings")).thenReturn(listingCache);
        
        when(appointmentRepository.findByListingIdInAndStatusIn(anyList(), anyList()))
                .thenReturn(List.of(appointment));
        when(listingBoostRepository.findActiveByListingIds(anyList()))
                .thenReturn(List.of(boost));
        
        when(engagementRepository.findByParticipantWithFetches(userId, null)).thenReturn(List.of(engagement));
        when(engagementRepository.findByListingIdInOrPropertyIdIn(anyList(), anyList()))
                .thenReturn(Collections.emptyList());
        
        when(agentProposalRepository.findByUserId(eq(userId), any())).thenReturn(new PageImpl<>(List.of(proposal)));

        // Act
        userApplicationService.suspendUser(userId);

        // Assert
        verify(user).suspend();
        verify(userRepository).save(user);
        
        verify(property).updateStatus(PropertyStatus.DRAFT);
        verify(propertyRepository).saveAll(anyList());
        
        verify(listing).unpublish();
        verify(listingRepository).saveAll(anyList());
        verify(listingCache).evict(listingId);
        
        verify(appointment).cancel(eq(userId), anyString());
        verify(appointmentRepository).saveAll(anyList());
        
        verify(boost).cancel();
        verify(listingBoostRepository).save(boost);
        
        verify(engagement).cancel(anyString());
        verify(engagementRepository).save(engagement);
        
        verify(proposal).setAsDraft();
        verify(agentProposalRepository).save(proposal);
    }

    @Test
    void suspendUser_withMixedListingStatuses_shouldOnlyUnpublishPublishedOnes() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = spy(User.builder().userId(userId).status(com.sep.realvista.domain.user.UserStatus.ACTIVE).build());

        // One published listing
        Listing publishedListing = spy(Listing.builder()
                .listingId(UUID.randomUUID())
                .status(com.sep.realvista.domain.listing.ListingStatus.PUBLISHED)
                .build());
        // One draft listing (should be ignored by Listing::unpublish)
        Listing draftListing = spy(Listing.builder()
                .listingId(UUID.randomUUID())
                .status(com.sep.realvista.domain.listing.ListingStatus.DRAFT)
                .build());

        when(userDomainService.getUserOrThrow(userId)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(propertyRepository.findByOwnerId(userId)).thenReturn(Collections.emptyList());
        when(listingRepository.findByUserIdOrPropertyOwnerId(userId)).thenReturn(List.of(publishedListing, draftListing));
        when(cacheManager.getCache("listings")).thenReturn(listingCache);
        when(engagementRepository.findByParticipantWithFetches(userId, null)).thenReturn(Collections.emptyList());
        when(engagementRepository.findByListingIdInOrPropertyIdIn(anyList(), anyList())).thenReturn(Collections.emptyList());
        when(agentProposalRepository.findByUserId(eq(userId), any())).thenReturn(new PageImpl<>(Collections.emptyList()));

        // Act
        userApplicationService.suspendUser(userId);

        // Assert
        verify(publishedListing, times(1)).unpublish();
        verify(draftListing, never()).unpublish();
        verify(listingRepository).saveAll(anyList());
        verify(listingCache).evict(publishedListing.getListingId());
        verify(listingCache).evict(draftListing.getListingId());
    }

    @Test
    void banUser_shouldCascadeCleanup() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = spy(User.builder().userId(userId).status(com.sep.realvista.domain.user.UserStatus.ACTIVE).build());

        UUID propertyId = UUID.randomUUID();
        Property property = spy(Property.builder().propertyId(propertyId).build());

        UUID listingId = UUID.randomUUID();
        Listing listing = spy(Listing.builder().listingId(listingId).status(com.sep.realvista.domain.listing.ListingStatus.PUBLISHED).build());

        Appointment appointment = spy(Appointment.builder().build());
        ListingBoost boost = spy(ListingBoost.builder().build());
        Engagement engagement = spy(Engagement.builder().status(EngagementStatus.SUBMITTED).build());
        AgentProposal proposal = spy(AgentProposal.builder()
                .status(com.sep.realvista.domain.engagement.proposal.AgentProposalStatus.ACTIVE).build());
        UserFeatureSubscription subscription = spy(UserFeatureSubscription.builder().build());

        when(userDomainService.getUserOrThrow(userId)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);

        when(propertyRepository.findByOwnerId(userId)).thenReturn(List.of(property));
        when(propertyAgentRepository.findByAgentId(userId)).thenReturn(Collections.emptyList());
        when(listingRepository.findByUserIdOrPropertyOwnerId(userId)).thenReturn(List.of(listing));
        when(cacheManager.getCache("listings")).thenReturn(listingCache);

        when(appointmentRepository.findByListingIdInAndStatusIn(anyList(), anyList()))
                .thenReturn(List.of(appointment));
        when(listingBoostRepository.findActiveByListingIds(anyList()))
                .thenReturn(List.of(boost));

        when(engagementRepository.findByParticipantWithFetches(userId, null)).thenReturn(List.of(engagement));
        when(engagementRepository.findByListingIdInOrPropertyIdIn(anyList(), anyList()))
                .thenReturn(Collections.emptyList());

        when(agentProposalRepository.findByUserId(eq(userId), any())).thenReturn(new PageImpl<>(List.of(proposal)));
        when(userFeatureSubscriptionRepository.findAllActiveByUserId(userId)).thenReturn(List.of(subscription));

        // Act
        userApplicationService.banUser(userId);

        // Assert
        verify(user).ban();
        verify(userRepository).save(user);

        verify(property).updateStatus(PropertyStatus.DRAFT);
        verify(propertyRepository).saveAll(anyList());

        verify(listing).unpublish();
        verify(listingRepository).saveAll(anyList());
        verify(listingCache).evict(listingId);

        verify(appointment).cancel(eq(userId), eq("Tài khoản chủ nhà đã bị khóa"));
        verify(appointmentRepository).saveAll(anyList());

        verify(boost).cancel();
        verify(listingBoostRepository).save(boost);

        verify(engagement).cancel(eq("Tài khoản người dùng đã bị khóa"));
        verify(engagementRepository).save(engagement);

        verify(proposal).setAsDraft();
        verify(agentProposalRepository).save(proposal);

        verify(subscription).cancel();
        verify(userFeatureSubscriptionRepository).save(subscription);
    }

    @Test
    void deleteUser_shouldCascadeCleanupAndRemoveAgentAssignments() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID assignedPropertyId = UUID.randomUUID();
        User user = spy(User.builder().userId(userId).status(com.sep.realvista.domain.user.UserStatus.ACTIVE).build());
        PropertyAgent propertyAgent = PropertyAgent.builder()
                .propertyId(assignedPropertyId)
                .agentId(userId)
                .build();
        Engagement engagement = spy(Engagement.builder().status(EngagementStatus.ACCEPTED).build());
        UserFeatureSubscription subscription = spy(UserFeatureSubscription.builder().build());

        when(userDomainService.getUserOrThrow(userId)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(propertyRepository.findByOwnerId(userId)).thenReturn(Collections.emptyList());
        when(propertyAgentRepository.findByAgentId(userId)).thenReturn(List.of(propertyAgent));
        when(listingRepository.findByUserIdOrPropertyOwnerId(userId)).thenReturn(Collections.emptyList());
        when(engagementRepository.findByParticipantWithFetches(userId, null)).thenReturn(List.of(engagement));
        when(engagementRepository.findByListingIdInOrPropertyIdIn(anyList(), anyList()))
                .thenReturn(Collections.emptyList());
        when(agentProposalRepository.findByUserId(eq(userId), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        when(userFeatureSubscriptionRepository.findAllActiveByUserId(userId)).thenReturn(List.of(subscription));

        // Act
        userApplicationService.deleteUser(userId);

        // Assert
        verify(user).markAsDeleted();
        verify(propertyAgentRepository).deleteAll(List.of(propertyAgent));
        verify(engagement).cancel(eq("Tài khoản người dùng đã bị xóa"));
        verify(subscription).cancel();
    }
}
