package com.sep.realvista.domain.engagement;

import com.sep.realvista.domain.common.entity.BaseEntity;
import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "engagements", indexes = {
        @Index(name = "idx_engagement_initiator", columnList = "initiator_id"),
        @Index(name = "idx_engagement_receiver", columnList = "receiver_id"),
        @Index(name = "idx_engagement_type", columnList = "engagement_type"),
        @Index(name = "idx_engagement_listing", columnList = "listing_id"),
        @Index(name = "idx_engagement_property", columnList = "property_id"),
        @Index(name = "idx_engagement_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString(exclude = {"initiator", "receiver", "property"})
public class Engagement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "engagement_id")
    private UUID engagementId;

    @Column(name = "initiator_id", nullable = false)
    private UUID initiatorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", insertable = false, updatable = false)
    private User initiator;

    @Column(name = "receiver_id", nullable = false)
    private UUID receiverId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", insertable = false, updatable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(name = "engagement_type", nullable = false, length = 30)
    private EngagementType engagementType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private String content;

    @Column(name = "listing_id")
    private UUID listingId;

    @Column(name = "property_id")
    private UUID propertyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", insertable = false, updatable = false)
    private Property property;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EngagementStatus status = EngagementStatus.SUBMITTED;

    public void accept() {
        this.status = EngagementStatus.ACCEPTED;
    }

    public void reject() {
        this.status = EngagementStatus.REJECTED;
    }

    public void cancel() {
        this.status = EngagementStatus.CANCELLED;
    }
}
