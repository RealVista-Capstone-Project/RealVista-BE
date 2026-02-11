package com.sep.realvista.integration.application.listing;

import com.sep.realvista.application.listing.dto.ListingSearchResponse;
import com.sep.realvista.application.listing.service.ListingSearchService;
import com.sep.realvista.domain.common.value.Email;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.PropertyCategory;
import com.sep.realvista.domain.property.PropertyStatus;
import com.sep.realvista.domain.property.PropertyType;
import com.sep.realvista.domain.property.location.Location;
import com.sep.realvista.domain.property.location.LocationType;
import com.sep.realvista.domain.property.repository.PropertyCategoryRepository;
import com.sep.realvista.domain.property.PropertyRepository;
import com.sep.realvista.domain.property.repository.PropertyTypeRepository;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserRepository;
import com.sep.realvista.domain.user.UserStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ListingSearchService Integration Tests")
class ListingSearchServiceIntegrationTest {

    @Autowired
    private ListingSearchService listingSearchService;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private PropertyTypeRepository propertyTypeRepository;

    @Autowired
    private PropertyCategoryRepository propertyCategoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private PropertyType apartmentType;
    private PropertyCategory residentialCategory;
    private Listing listing1;
    private Listing listing2;
    private User testUser;
    private Location testLocation;

    @BeforeEach
    void setUp() {
        // Register H2 function alias for JSONB
        try {
            entityManager.createNativeQuery("CREATE ALIAS IF NOT EXISTS jsonb_extract_path_text FOR \"com.sep.realvista.integration.H2JsonFunctions.jsonbExtractPathText\"").executeUpdate();
        } catch (Exception e) {
            // Ignore if already exists or fails
            System.out.println("Warning: Failed to create H2 alias: " + e.getMessage());
        }

        // Clear data
        listingRepository.deleteAll();
        propertyRepository.deleteAll();
        propertyTypeRepository.deleteAll();
        propertyCategoryRepository.deleteAll();
        
        // Clear locations using JPQL because no repository
        entityManager.createQuery("DELETE FROM Location").executeUpdate();
        userRepository.deleteAll();

        // Create User
        testUser = User.builder()
                .email(Email.of("test@example.com"))
                .businessName("Test Agent")
                .passwordHash("hashedpassword")
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(testUser);

        // Create Location
        testLocation = Location.builder()
                .name("Ho Chi Minh City")
                .code("HCMC")
                .type(LocationType.CITY)
                .northLat(BigDecimal.valueOf(11.0))
                .southLat(BigDecimal.valueOf(10.0))
                .eastLng(BigDecimal.valueOf(107.0))
                .westLng(BigDecimal.valueOf(106.0))
                .build();
        entityManager.persist(testLocation);
        entityManager.flush();

        // underlying data
        residentialCategory = PropertyCategory.builder()
                .code("RES")
                .name("Residential")
                .build();
        propertyCategoryRepository.save(residentialCategory);

        apartmentType = PropertyType.builder()
                .code("APT")
                .name("Apartment")
                .propertyCategory(residentialCategory)
                .build();
        propertyTypeRepository.save(apartmentType);

        // Property 1: Apartment with 2 beds, 2 baths, 100m2, South facing, Has Pool
        Map<String, Object> extraAttrs1 = new HashMap<>();
        extraAttrs1.put("direction", "South");
        extraAttrs1.put("hasPool", "true");
        
        Property property1 = createProperty("123 Main St", 100.0, 2, 2, apartmentType, extraAttrs1);
        listing1 = createListing(property1, "Luxury Apt", 2000.0);
        
        // Property 2: Apartment with 3 beds, 2 baths, 150m2, North facing, No Pool
        Map<String, Object> extraAttrs2 = new HashMap<>();
        extraAttrs2.put("direction", "North");
        extraAttrs2.put("hasPool", "false");

        Property property2 = createProperty("456 High St", 150.0, 3, 2, apartmentType, extraAttrs2);
        listing2 = createListing(property2, "Spacious Apt", 3000.0);
        
        entityManager.flush();
    }

    private Property createProperty(String address, Double area, Integer beds, Integer baths, PropertyType type, Map<String, Object> extraAttributes) {
        Property property = Property.builder()
                .streetAddress(address)
                .usableSizeM2(BigDecimal.valueOf(area))
                .bedrooms(beds)
                .bathrooms(baths)
                .descriptions("Test description")
                .propertyType(type)
                .latitude(BigDecimal.ZERO)
                .longitude(BigDecimal.ZERO)
                .ownerId(testUser.getUserId())
                .locationId(testLocation.getLocationId())
                .extraAttributes(extraAttributes)
                .status(PropertyStatus.AVAILABLE)
                .landSizeM2(BigDecimal.ZERO)
                .widthM(BigDecimal.ZERO)
                .lengthM(BigDecimal.ZERO)
                .build();
        return propertyRepository.save(property);
    }

    private Listing createListing(Property property, String name, Double price) {
        Listing listing = Listing.builder()
                .propertyId(property.getPropertyId())
                .property(property)
                .userId(property.getOwnerId())
                .listingType(ListingType.RENT)
                .status(ListingStatus.PUBLISHED)
                .name(name)
                .slug(name.toLowerCase().replace(" ", "-"))
                .price(BigDecimal.valueOf(price))
                .publishedAt(LocalDateTime.now())
                .build();
        return listingRepository.save(listing);
    }

    @Test
    void search_basicFilters_shouldReturnMatchingListings() {
        // Search by price range
        Page<ListingSearchResponse> result = listingSearchService.search(
                "RENT", null, null, null,
                BigDecimal.valueOf(1500), BigDecimal.valueOf(2500), // Min 1500, Max 2500
                null, null, null, null, null, null,
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getListingId()).isEqualTo(listing1.getListingId());
    }

    @Test
    void search_dynamicAttribute_text_shouldReturnMatchingListings() {
        Map<String, String> dynamicFilters = new HashMap<>();
        dynamicFilters.put("direction", "South");

        Page<ListingSearchResponse> result = listingSearchService.search(
                "RENT", null, null, null,
                null, null, null, null, null, null,
                dynamicFilters, null,
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getListingId()).isEqualTo(listing1.getListingId());
    }

    @Test
    void search_dynamicAttribute_boolean_shouldReturnMatchingListings() {
        Map<String, String> dynamicFilters = new HashMap<>();
        dynamicFilters.put("hasPool", "true");

        Page<ListingSearchResponse> result = listingSearchService.search(
                "RENT", null, null, null,
                null, null, null, null, null, null,
                dynamicFilters, null,
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getListingId()).isEqualTo(listing1.getListingId());
    }

    @Test
    void search_combinedFilters_shouldReturnMatchingListings() {
        Map<String, String> dynamicFilters = new HashMap<>();
        dynamicFilters.put("direction", "North");

        Page<ListingSearchResponse> result = listingSearchService.search(
                "RENT", null, null, null,
                BigDecimal.valueOf(2500), BigDecimal.valueOf(3500), // Price range matches listing2
                120.0, 200.0, // Area match listing2 (150)
                3, null, // Bedrooms match listing2
                dynamicFilters, null,
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getListingId()).isEqualTo(listing2.getListingId());
    }
}
