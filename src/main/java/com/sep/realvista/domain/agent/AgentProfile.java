package com.sep.realvista.domain.agent;

import com.sep.realvista.domain.common.entity.BaseEntity;
import com.sep.realvista.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "agent_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AgentProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "agent_profile_id")
    private UUID agentProfileId;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(columnDefinition = "TEXT")
    private String specialties;

    @Column(name = "service_areas", columnDefinition = "TEXT")
    private String serviceAreas;

    @Column(precision = 2, scale = 1)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "properties_sold")
    @Builder.Default
    private Integer propertiesSold = 0;

    @OneToMany(mappedBy = "agentProfile", fetch = FetchType.LAZY)
    @Builder.Default
    private List<AgentLicense> licenses = new ArrayList<>();

    public void updateBio(String bio) {
        this.bio = bio;
    }

    public void updateSpecialties(String specialties) {
        this.specialties = specialties;
    }

    public void updateServiceAreas(String serviceAreas) {
        this.serviceAreas = serviceAreas;
    }

    public void updateYearsOfExperience(Integer years) {
        if (years != null && years < 0) {
            throw new IllegalArgumentException("Years of experience must be >= 0");
        }
        this.yearsOfExperience = years;
    }

    public void incrementPropertiesSold() {
        this.propertiesSold++;
    }

    public void updateRating(BigDecimal newRating) {
        if (newRating != null && newRating.compareTo(BigDecimal.ZERO) >= 0 
                && newRating.compareTo(new BigDecimal("5.0")) <= 0) {
            this.rating = newRating;
        }
    }
}
