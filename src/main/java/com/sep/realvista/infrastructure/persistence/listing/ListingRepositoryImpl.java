package com.sep.realvista.infrastructure.persistence.listing;

import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
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
    public List<Listing> findAll(Specification<Listing> spec) {
        return jpaRepository.findAll(spec);
    }
}
