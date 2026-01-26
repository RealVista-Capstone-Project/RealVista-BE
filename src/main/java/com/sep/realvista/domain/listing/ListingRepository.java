package com.sep.realvista.domain.listing;

import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ListingRepository {
    Listing save(Listing listing);
    Optional<Listing> findById(UUID id);
    void deleteById(UUID id);
    List<Listing> findAll(Specification<Listing> spec);
}
