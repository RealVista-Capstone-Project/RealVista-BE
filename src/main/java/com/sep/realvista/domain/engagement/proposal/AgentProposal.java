package com.sep.realvista.domain.engagement.proposal;

import com.sep.realvista.domain.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "agent_proposals", indexes = {
        @Index(name = "idx_agent_proposal_user", columnList = "user_id"),
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

    @Column(name = "specialty")
    private UUID specialty;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "price_range", columnDefinition = "jsonb")
    private Map<String, Object> priceRange;

    public void activate() {
        this.status = AgentProposalStatus.ACTIVE;
    }

    public void archive() {
        this.status = AgentProposalStatus.ARCHIVED;
    }
    
    public void setAsDraft() {
        this.status = AgentProposalStatus.DRAFT;
    }

    public void update(String title, java.math.BigDecimal commissionRate, 
                       Integer experienceYears, String pitchContent,
                       UUID specialty, Map<String, Object> priceRange) {
        if (title != null) {
            this.title = title;
        }
        if (commissionRate != null) {
            this.commissionRate = commissionRate;
        }
        if (experienceYears != null) {
            this.experienceYears = experienceYears;
        }
        if (pitchContent != null) {
            this.pitchContent = pitchContent;
        }
        if (specialty != null) {
            this.specialty = specialty;
        }
        if (priceRange != null) {
            this.priceRange = priceRange;
        }
    }
}
