package com.sep.realvista.domain.agent;

import com.sep.realvista.domain.common.entity.BaseEntity;
import com.sep.realvista.domain.property.Property;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "property_agents",
        indexes = {
                @Index(name = "idx_property_agent_property", columnList = "property_id"),
                @Index(name = "idx_property_agent_agent", columnList = "agent_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"property_id", "agent_id"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PropertyAgent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "property_agent_id")
    private UUID propertyAgentId;

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", insertable = false, updatable = false)
    private Property property;

    @Column(name = "agent_id", nullable = false)
    private UUID agentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private AgentProfile agentProfile;
}
