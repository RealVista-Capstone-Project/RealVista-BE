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

    @Column(name = "listing_id", nullable = false)
    private UUID listingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", insertable = false, updatable = false)
    private Listing listing;

    @Column(name = "buyer_id", nullable = false)
    private UUID buyerId;

    @Column(name = "conversation_id")
    private UUID conversationId;

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

    public void contact() {
        this.status = LeadStatus.CONTACTED;
        this.lastContactedAt = LocalDateTime.now();
    }

    public void qualify() {
        this.status = LeadStatus.QUALIFIED;
    }

    public void negotiate() {
        this.status = LeadStatus.NEGOTIATING;
    }

    public void closeWon() {
        this.status = LeadStatus.CLOSED_WON;
    }

    public void closeLost() {
        this.status = LeadStatus.CLOSED_LOST;
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
