package com.sep.realvista.integration.application.listing;

import com.sep.realvista.application.listing.dto.ListingSearchResponse;
import com.sep.realvista.application.listing.dto.ListingSearchCriteria;
import com.sep.realvista.application.listing.service.ListingSearchService;
import com.sep.realvista.domain.common.value.Email;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingStatus;
import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.domain.listing.repository.ListingRepository;
import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.PropertyCategory;
import com.sep.realvista.domain.property.repository.PropertyCategoryRepository;
import com.sep.realvista.domain.property.PropertyStatus;
import com.sep.realvista.domain.property.PropertyType;
import com.sep.realvista.domain.property.repository.PropertyTypeRepository;
import com.sep.realvista.domain.property.attribute.AttributeDataType;
import com.sep.realvista.domain.property.attribute.PropertyAttribute;
import com.sep.realvista.domain.property.attribute.PropertyAttributeValue;
import com.sep.realvista.domain.property.location.Location;
import com.sep.realvista.domain.property.location.LocationType;
import com.sep.realvista.domain.property.repository.PropertyRepository;
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
import java.util.List;
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
    private PropertyAttribute bedroomsAttribute;
    private PropertyAttribute bathroomsAttribute;
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
        entityManager.createQuery("DELETE FROM PropertyAttributeValue").executeUpdate();
        propertyRepository.deleteAll();
        propertyTypeRepository.deleteAll();
        propertyCategoryRepository.deleteAll();
        entityManager.createQuery("DELETE FROM PropertyAttribute").executeUpdate();

        // Clear locations using JPQL because no repository
        entityManager.createQuery("DELETE FROM Location").executeUpdate();

        // Create User
        testUser = User.builder()
                .email(Email.of("test@example.com"))
                .businessName("Test Agent")
                .passwordHash("hashedpassword")
                .status(UserStatus.ACTIVE)
                .build();
        testUser = userRepository.save(testUser);

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
        residentialCategory = propertyCategoryRepository.save(residentialCategory);

        apartmentType = PropertyType.builder()
                .code("APT")
                .name("Apartment")
                .propertyCategoryId(residentialCategory.getPropertyCategoryId())
                .name("Apartment")
                .propertyCategory(residentialCategory)
                .build();
        apartmentType = propertyTypeRepository.save(apartmentType);

        // Create property attribute definitions (BEDROOMS, BATHROOMS)
        bedroomsAttribute = PropertyAttribute.builder()
                .name("Bedrooms")
                .code("BEDROOMS")
                .dataType(AttributeDataType.NUMBER)
                .isSearchable(true)
                .unit("rooms")
                .build();
        entityManager.persist(bedroomsAttribute);

        bathroomsAttribute = PropertyAttribute.builder()
                .name("Bathrooms")
                .code("BATHROOMS")
                .dataType(AttributeDataType.NUMBER)
                .isSearchable(true)
                .unit("rooms")
                .build();
        entityManager.persist(bathroomsAttribute);
        entityManager.flush();

        // Property 1: Apartment with 2 beds, 2 baths, 100m2, South facing, Has Pool
        Map<String, Object> extraAttrs1 = new HashMap<>();
        extraAttrs1.put("direction", "South");
        extraAttrs1.put("hasPool", "true");
        
        Property property1 = createProperty("123 Main St", 100.0, apartmentType, extraAttrs1);
        createAttributeValue(property1, bedroomsAttribute, 2);
        createAttributeValue(property1, bathroomsAttribute, 2);
        listing1 = createListing(property1, "Luxury Apt", 2000.0);
        
        // Property 2: Apartment with 3 beds, 2 baths, 150m2, North facing, No Pool
        Map<String, Object> extraAttrs2 = new HashMap<>();
        extraAttrs2.put("direction", "North");
        extraAttrs2.put("hasPool", "false");

        Property property2 = createProperty("456 High St", 150.0, apartmentType, extraAttrs2);
        createAttributeValue(property2, bedroomsAttribute, 3);
        createAttributeValue(property2, bathroomsAttribute, 2);
        listing2 = createListing(property2, "Spacious Apt", 3000.0);
        
        entityManager.flush();
    }

    private Property createProperty(String address, Double area, PropertyType type, Map<String, Object> extraAttributes) {
        Property property = Property.builder()
                .streetAddress(address)
                .usableSizeM2(BigDecimal.valueOf(area))
                .descriptions("Test description")
                .propertyTypeId(type.getPropertyTypeId())
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

    private void createAttributeValue(Property property, PropertyAttribute attribute, int value) {
        PropertyAttributeValue pav = PropertyAttributeValue.builder()
                .propertyId(property.getPropertyId())
                .property(property)
                .propertyAttributeId(attribute.getPropertyAttributeId())
                .propertyAttribute(attribute)
                .valueNumber(BigDecimal.valueOf(value))
                .build();
        entityManager.persist(pav);
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
    void debug_json_storage() {
        List<Object[]> results = entityManager.createNativeQuery("SELECT property_id, extra_attributes FROM properties").getResultList();
        System.out.println("DEBUG: Found " + results.size() + " properties");
        for (Object[] row : results) {
            Object jsonValue = row[1];
            String content = "null";
            if (jsonValue instanceof byte[] bytes) {
                content = new String(bytes);
            } else if (jsonValue != null) {
                content = jsonValue.toString();
            }
            System.out.println("Property " + row[0] + " extra_attributes: " + content + " (type: " + (jsonValue != null ? jsonValue.getClass().getName() : "null") + ")");
        }
    }

    @Test
    void search_basicFilters_shouldReturnMatchingListings() {
        // Search by price range
        ListingSearchCriteria criteria = ListingSearchCriteria.builder()
                .listingType("RENT")
                .minPrice(BigDecimal.valueOf(1500))
                .maxPrice(BigDecimal.valueOf(2500))
                .build();

        Page<ListingSearchResponse> result = listingSearchService.search(
                criteria,
                PageRequest.of(0, 10),
                null
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getListingId()).isEqualTo(listing1.getListingId());
    }

    @Test
    void search_dynamicAttribute_text_shouldReturnMatchingListings() {
        Map<String, String> dynamicFilters = new HashMap<>();
        dynamicFilters.put("direction", "South");

        ListingSearchCriteria criteria = ListingSearchCriteria.builder()
                .listingType("RENT")
                .dynamicAttributes(dynamicFilters)
                .build();

        Page<ListingSearchResponse> result = listingSearchService.search(
                criteria,
                PageRequest.of(0, 10),
                null
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getListingId()).isEqualTo(listing1.getListingId());
    }

    @Test
    void search_dynamicAttribute_boolean_shouldReturnMatchingListings() {
        Map<String, String> dynamicFilters = new HashMap<>();
        dynamicFilters.put("hasPool", "true");

        ListingSearchCriteria criteria = ListingSearchCriteria.builder()
                .listingType("RENT")
                .dynamicAttributes(dynamicFilters)
                .build();

        Page<ListingSearchResponse> result = listingSearchService.search(
                criteria,
                PageRequest.of(0, 10),
                null
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getListingId()).isEqualTo(listing1.getListingId());
    }

    @Test
    void search_combinedFilters_shouldReturnMatchingListings() {
        Map<String, String> dynamicFilters = new HashMap<>();
        dynamicFilters.put("direction", "North");
        dynamicFilters.put("BEDROOMS", "3");

        ListingSearchCriteria criteria = ListingSearchCriteria.builder()
                .listingType("RENT")
                .minPrice(BigDecimal.valueOf(2500))
                .maxPrice(BigDecimal.valueOf(3500))
                .minArea(120.0)
                .maxArea(200.0)
                .dynamicAttributes(dynamicFilters)
                .build();

        Page<ListingSearchResponse> result = listingSearchService.search(
                criteria,
                PageRequest.of(0, 10),
                null
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getListingId()).isEqualTo(listing2.getListingId());
    }

    @Test
    @DisplayName("Should filter by bedrooms attribute value")
    void search_bedroomsFilter_shouldReturnMatchingListings() {
        // Property 1 has 2 bedrooms, Property 2 has 3 bedrooms
        // Search for >= 3 bedrooms should return only listing2
        Map<String, String> dynamicFilters = new HashMap<>();
        dynamicFilters.put("BEDROOMS", "3");
        ListingSearchCriteria criteria = ListingSearchCriteria.builder()
                .listingType("RENT")
                .dynamicAttributes(dynamicFilters)
                .build();

        Page<ListingSearchResponse> result = listingSearchService.search(
                criteria,
                PageRequest.of(0, 10),
                null
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getListingId()).isEqualTo(listing2.getListingId());
    }

    @Test
    @DisplayName("Should filter by bathrooms attribute value")
    void search_bathroomsFilter_shouldReturnMatchingListings() {
        // Both properties have 2 bathrooms
        // Search for >= 2 bathrooms should return both
        Map<String, String> dynamicFilters = new HashMap<>();
        dynamicFilters.put("BATHROOMS", "2");
        ListingSearchCriteria criteria = ListingSearchCriteria.builder()
                .listingType("RENT")
                .dynamicAttributes(dynamicFilters)
                .build();

        Page<ListingSearchResponse> result = listingSearchService.search(
                criteria,
                PageRequest.of(0, 10),
                null
        );

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Should filter by both bedrooms and bathrooms")
    void search_bedroomsAndBathroomsFilter_shouldReturnMatchingListings() {
        // Search for >= 2 bedrooms AND >= 2 bathrooms: both match
        Map<String, String> dynamicFilters = new HashMap<>();
        dynamicFilters.put("BEDROOMS", "2");
        dynamicFilters.put("BATHROOMS", "2");
        ListingSearchCriteria criteria = ListingSearchCriteria.builder()
                .listingType("RENT")
                .dynamicAttributes(dynamicFilters)
                .build();

        Page<ListingSearchResponse> result = listingSearchService.search(
                criteria,
                PageRequest.of(0, 10),
                null
        );

        assertThat(result.getContent()).hasSize(2);
    }
}
