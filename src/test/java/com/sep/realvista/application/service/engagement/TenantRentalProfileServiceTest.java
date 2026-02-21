package com.sep.realvista.application.service.engagement;

import com.sep.realvista.application.engagement.mapper.TenantRentalProfileMapper;
import com.sep.realvista.application.listing.dto.TenantRentalProfileDto;
import com.sep.realvista.domain.engagement.rental.TenantRentalProfile;
import com.sep.realvista.domain.engagement.rental.TenantRentalProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantRentalProfileServiceTest {

    @Mock
    private TenantRentalProfileRepository tenantRentalProfileRepository;

    @Mock
    private TenantRentalProfileMapper tenantRentalProfileMapper;

    @InjectMocks
    private TenantRentalProfileService tenantRentalProfileService;

    @Test
    void getMyProfiles_ShouldReturnDtoList() {
        // Arrange
        UUID userId = UUID.randomUUID();
        TenantRentalProfile profile = TenantRentalProfile.builder()
                .profileId(UUID.randomUUID())
                .userId(userId)
                .title("Default Profile")
                .build();

        TenantRentalProfileDto dto = TenantRentalProfileDto.builder()
                .profileId(profile.getProfileId())
                .title("Default Profile")
                .build();

        when(tenantRentalProfileRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(profile));
        when(tenantRentalProfileMapper.toDto(profile)).thenReturn(dto);

        // Act
        List<TenantRentalProfileDto> result = tenantRentalProfileService.getMyProfiles(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Default Profile", result.get(0).getTitle());
        verify(tenantRentalProfileRepository).findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId);
    }
}
