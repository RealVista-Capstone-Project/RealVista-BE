package com.sep.realvista.domain.agent.lead;

import com.sep.realvista.domain.common.entity.BaseEntity;
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

import java.util.UUID;

@Entity
@Table(name = "lead_notes", indexes = {
        @Index(name = "idx_lead_note_lead", columnList = "listing_lead_id"),
        @Index(name = "idx_lead_note_type", columnList = "note_type")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class LeadNote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "lead_note_id")
    private UUID leadNoteId;

    @Column(name = "listing_lead_id", nullable = false)
    private UUID listingLeadId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_lead_id", insertable = false, updatable = false)
    private ListingLead listingLead;

    @Enumerated(EnumType.STRING)
    @Column(name = "note_type", nullable = false, length = 20)
    private NoteType noteType;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private LeadTag tag;

    @Column(columnDefinition = "TEXT")
    private String content;

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateTag(LeadTag tag) {
        this.tag = tag;
    }

    public void updateNoteType(NoteType noteType) {
        if (noteType == null) {
            throw new IllegalArgumentException("Note type cannot be null");
        }
        this.noteType = noteType;
    }
}
