package com.sep.realvista.domain.engagement;

import com.sep.realvista.domain.common.entity.BaseEntity;
import com.sep.realvista.domain.property.Property;
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

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "agent_proposals", indexes = {
        @Index(name = "idx_agent_proposal_user", columnList = "user_id"),
        @Index(name = "idx_agent_proposal_property", columnList = "property_id"),
        @Index(name = "idx_agent_proposal_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AgentProposal extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "agent_proposal_id")
    private UUID agentProposalId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", insertable = false, updatable = false)
    private Property property;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "commission_rate", precision = 5, scale = 2)
    private BigDecimal commissionRate;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AgentProposalStatus status = AgentProposalStatus.DRAFT;

    @Column(name = "pitch_content", columnDefinition = "TEXT")
    private String pitchContent;

    public void activate() {
        this.status = AgentProposalStatus.ACTIVE;
    }

    public void archive() {
        this.status = AgentProposalStatus.ARCHIVED;
    }
}
