package com.sep.realvista.unit.application.listing.contract;

import com.sep.realvista.application.listing.contract.LeaseAgreementApplicationService;
import com.sep.realvista.application.listing.contract.mapper.LeaseAgreementMapper;
import com.sep.realvista.application.notification.service.NotificationApplicationService;
import com.sep.realvista.application.service.DocuSignService;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.listing.contract.LeaseAgreement;
import com.sep.realvista.domain.listing.contract.LeaseAgreementRepository;
import com.sep.realvista.domain.listing.contract.LeaseStatus;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.user.notification.EntityType;
import com.sep.realvista.domain.user.notification.EventType;
import com.sep.realvista.domain.property.repository.PropertyMediaRepository;
import com.sep.realvista.domain.property.repository.PropertyRepository;
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.infrastructure.config.DocuSignConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaseAgreementApplicationServiceTest {

    @Mock LeaseAgreementRepository leaseAgreementRepository;
    @Mock PropertyRepository propertyRepository;
    @Mock PropertyMediaRepository propertyMediaRepository;
    @Mock ListingRepository listingRepository;
    @Mock UserRepository userRepository;
    @Mock DocuSignService docuSignService;
    @Mock DocuSignConfig docuSignConfig;
    @Mock LeaseAgreementMapper leaseAgreementMapper;
    @Mock RestTemplate restTemplate;
    @Mock NotificationApplicationService notificationService;
    @Mock CacheManager cacheManager;

    LeaseAgreementApplicationService service;

    @BeforeEach
    void setUp() {
        service = new LeaseAgreementApplicationService(
                leaseAgreementRepository,
                propertyRepository,
                propertyMediaRepository,
                listingRepository,
                userRepository,
                docuSignService,
                docuSignConfig,
                leaseAgreementMapper,
                restTemplate,
                notificationService,
                cacheManager
        );
    }

    @Test
    @DisplayName("expireActiveLeasesPastEndDate expires only leases returned by active past-end query")
    void expireActiveLeasesPastEndDate_expiresActivePastEndLeases() {
        LocalDate today = LocalDate.now();
        LeaseAgreement pastLease = activeLease(today.minusDays(1));
        when(leaseAgreementRepository.findActiveLeasesEndingBefore(today))
                .thenReturn(List.of(pastLease));
        when(leaseAgreementRepository.save(any(LeaseAgreement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.expireActiveLeasesPastEndDate();

        assertThat(pastLease.getStatus()).isEqualTo(LeaseStatus.EXPIRED);
        verify(leaseAgreementRepository).save(pastLease);
        verify(propertyRepository, never()).save(any());
    }

    @Test
    @DisplayName("expireActiveLeasesPastEndDate is idempotent when no active past-end leases are found")
    void expireActiveLeasesPastEndDate_doesNothingWhenNoMatches() {
        LocalDate today = LocalDate.now();
        when(leaseAgreementRepository.findActiveLeasesEndingBefore(today))
                .thenReturn(List.of());

        service.expireActiveLeasesPastEndDate();

        verify(leaseAgreementRepository, never()).save(any());
        verify(propertyRepository, never()).save(any());
    }

    @Test
    @DisplayName("sendLeaseExpiryReminders sends 30-day reminder once")
    void sendLeaseExpiryReminders_sendsThirtyDayReminderOnce() {
        LocalDate today = LocalDate.now();
        LeaseAgreement lease = activeLease(today.plusDays(30));
        when(leaseAgreementRepository.findActiveLeasesEndingOn(today.plusDays(30))).thenReturn(List.of(lease));
        when(leaseAgreementRepository.findActiveLeasesEndingOn(today.plusDays(7))).thenReturn(List.of());
        when(leaseAgreementRepository.findActiveLeasesEndingOn(today)).thenReturn(List.of());
        when(leaseAgreementRepository.save(any(LeaseAgreement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.sendLeaseExpiryReminders();
        service.sendLeaseExpiryReminders();

        verify(notificationService).sendDbNotification(
                eq(lease.getLandlordId()),
                eq("LEASE_EXPIRY_REMINDER"),
                eq("vi"),
                any(),
                eq(EventType.LEASE_EXPIRY_REMINDER),
                eq(EntityType.LEASE),
                eq(lease.getLeaseAgreementId())
        );
        verify(notificationService).sendDbNotification(
                eq(lease.getRenterId()),
                eq("LEASE_EXPIRY_REMINDER"),
                eq("vi"),
                any(),
                eq(EventType.LEASE_EXPIRY_REMINDER),
                eq(EntityType.LEASE),
                eq(lease.getLeaseAgreementId())
        );
        verify(leaseAgreementRepository).save(lease);
    }

    @Test
    @DisplayName("sendLeaseExpiryReminders ignores already sent reminders")
    void sendLeaseExpiryReminders_ignoresAlreadySentReminder() {
        LocalDate today = LocalDate.now();
        LeaseAgreement lease = activeLease(today.plusDays(7));
        lease.markExpiryReminderSent(7);
        when(leaseAgreementRepository.findActiveLeasesEndingOn(today.plusDays(30))).thenReturn(List.of());
        when(leaseAgreementRepository.findActiveLeasesEndingOn(today.plusDays(7))).thenReturn(List.of(lease));
        when(leaseAgreementRepository.findActiveLeasesEndingOn(today)).thenReturn(List.of());

        service.sendLeaseExpiryReminders();

        verify(notificationService, never()).sendDbNotification(any(), any(), any(), any(), any(), any(), any());
        verify(leaseAgreementRepository, never()).save(any());
    }

    @Test
    @DisplayName("rejectLease rejects ACTIVE leases")
    void rejectLease_rejectsActiveLease() {
        UUID leaseId = UUID.randomUUID();
        when(leaseAgreementRepository.findById(leaseId)).thenReturn(Optional.of(activeLease(LocalDate.now().plusMonths(1))));

        assertThatThrownBy(() -> service.rejectLease(leaseId, "changed mind"))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("Only a DRAFT or pending signing lease can be rejected");

        verify(leaseAgreementRepository, never()).save(any());
    }

    @Test
    @DisplayName("rejectLease rejects EXPIRED leases")
    void rejectLease_rejectsExpiredLease() {
        UUID leaseId = UUID.randomUUID();
        LeaseAgreement expiredLease = activeLease(LocalDate.now().minusDays(1));
        expiredLease.expire();
        when(leaseAgreementRepository.findById(leaseId)).thenReturn(Optional.of(expiredLease));

        assertThatThrownBy(() -> service.rejectLease(leaseId, "too late"))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("Only a DRAFT or pending signing lease can be rejected");

        verify(leaseAgreementRepository, never()).save(any());
    }

    private LeaseAgreement activeLease(LocalDate leaseEndDate) {
        return LeaseAgreement.builder()
                .leaseAgreementId(UUID.randomUUID())
                .propertyId(UUID.randomUUID())
                .renterId(UUID.randomUUID())
                .landlordId(UUID.randomUUID())
                .leaseStartDate(leaseEndDate.minusMonths(1))
                .leaseEndDate(leaseEndDate)
                .leaseDurationMonths(1)
                .status(LeaseStatus.ACTIVE)
                .build();
    }
}
