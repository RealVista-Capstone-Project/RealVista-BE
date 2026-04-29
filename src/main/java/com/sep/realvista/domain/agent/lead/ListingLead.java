package com.sep.realvista.domain.agent.lead;

import com.sep.realvista.domain.common.entity.BaseEntity;
import com.sep.realvista.domain.listing.Listing;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "listing_leads", indexes = {
        @Index(name = "idx_listing_lead_agent", columnList = "agent_id"),
        @Index(name = "idx_listing_lead_listing", columnList = "listing_id"),
        @Index(name = "idx_listing_lead_buyer", columnList = "buyer_id"),
        @Index(name = "idx_listing_lead_status", columnList = "status"),
        @Index(name = "idx_listing_lead_priority", columnList = "priority")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString(exclude = {"listing"})
public class ListingLead extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "listing_lead_id")
    private UUID listingLeadId;

    @Column(name = "agent_id", nullable = false)
    private UUID agentId;

    @Column(name = "listing_id")
    private UUID listingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", insertable = false, updatable = false)
    private Listing listing;

    @Column(name = "buyer_id")
    private UUID buyerId;

    @Column(name = "conversation_id")
    private UUID conversationId;

    // CRM free-text contact info
    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    @Builder.Default
    private LeadSource source = LeadSource.MANUAL;

    @Column(name = "budget", precision = 20, scale = 2)
    private BigDecimal budget;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LeadStatus status = LeadStatus.NEW;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LeadPriority priority = LeadPriority.MEDIUM;

    @Column(name = "last_contacted_at")
    private LocalDateTime lastContactedAt;

    @Column(name = "next_follow_up_at")
    private LocalDateTime nextFollowUpAt;

    public void updateStatus(LeadStatus newStatus) {
        this.status = newStatus;
        if (newStatus == LeadStatus.CONSULTING) {
            this.lastContactedAt = LocalDateTime.now();
        }
    }

    public void update(String fullName, String email, String phone,
                       LeadSource source, UUID listingId, BigDecimal budget) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.source = source;
        this.listingId = listingId;
        this.budget = budget;
    }

    public void updatePriority(LeadPriority priority) {
        if (priority == null) {
            throw new IllegalArgumentException("Priority cannot be null");
        }
        this.priority = priority;
    }

    public void scheduleFollowUp(LocalDateTime followUpAt) {
        this.nextFollowUpAt = followUpAt;
    }
}
