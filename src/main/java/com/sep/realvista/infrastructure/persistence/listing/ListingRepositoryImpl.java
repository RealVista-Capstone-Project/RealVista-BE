package com.sep.realvista.infrastructure.persistence.listing;

import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ListingRepositoryImpl implements ListingRepository {

    private final ListingJpaRepository jpaRepository;

    @Override
    public Listing save(Listing listing) {
        return jpaRepository.save(listing);
    }

    @Override
    public Optional<Listing> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public Page<Listing> findAll(Specification<Listing> spec, Pageable pageable) {
        return jpaRepository.findAll(spec, pageable);
    }
}
