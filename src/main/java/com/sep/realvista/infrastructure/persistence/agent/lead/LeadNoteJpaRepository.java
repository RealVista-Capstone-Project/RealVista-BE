package com.sep.realvista.infrastructure.persistence.agent.lead;

import com.sep.realvista.domain.agent.lead.LeadNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface LeadNoteJpaRepository extends JpaRepository<LeadNote, UUID> {

    @Query("SELECT n FROM LeadNote n WHERE n.listingLeadId = :listingLeadId "
            + "AND n.deleted = false ORDER BY n.createdAt ASC")
    List<LeadNote> findAllByListingLeadId(@Param("listingLeadId") UUID listingLeadId);
}
