package com.sep.realvista.domain.agent;

import com.sep.realvista.domain.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "agent_reviews", indexes = {
        @Index(name = "idx_agent_review_profile", columnList = "agent_profile_id"),
        @Index(name = "idx_agent_review_reviewer", columnList = "reviewer_id"),
        @Index(name = "idx_agent_review_listing", columnList = "listing_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AgentReview extends BaseEntity {

    private static final BigDecimal MIN_RATING = BigDecimal.ZERO;
    private static final BigDecimal MAX_RATING = new BigDecimal("5.0");

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "agent_review_id")
    private UUID agentReviewId;

    @Column(name = "agent_profile_id", nullable = false)
    private UUID agentProfileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_profile_id", insertable = false, updatable = false)
    private AgentProfile agentProfile;

    @Column(name = "reviewer_id", nullable = false)
    private UUID reviewerId;

    @Column(name = "listing_id", nullable = false)
    private UUID listingId;

    @Column(columnDefinition = "TEXT")
    private String review;

    @Column(precision = 2, scale = 1)
    private BigDecimal rating;

    public void updateReview(String review) {
        this.review = review;
    }

    public void updateRating(BigDecimal newRating) {
        if (newRating == null) {
            throw new IllegalArgumentException("Rating cannot be null");
        }
        if (newRating.compareTo(MIN_RATING) < 0 || newRating.compareTo(MAX_RATING) > 0) {
            throw new IllegalArgumentException("Rating must be between 0 and 5");
        }
        this.rating = newRating;
    }

    public static boolean isValidRating(BigDecimal rating) {
        return rating != null
                && rating.compareTo(MIN_RATING) >= 0
                && rating.compareTo(MAX_RATING) <= 0;
    }
}
