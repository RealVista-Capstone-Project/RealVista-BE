package com.sep.realvista.application.billing;

import com.sep.realvista.application.billing.dto.admin.BoostPackageAdminResponse;
import com.sep.realvista.application.billing.dto.admin.CreateBoostPackageRequest;
import com.sep.realvista.application.billing.dto.admin.CreateFeaturePackageRequest;
import com.sep.realvista.application.billing.dto.admin.FeaturePackageAdminResponse;
import com.sep.realvista.application.billing.dto.admin.PackageSnapshotResponse;
import com.sep.realvista.application.billing.dto.admin.UpdateBoostPackageRequest;
import com.sep.realvista.application.billing.dto.admin.UpdateFeaturePackageRequest;
import com.sep.realvista.domain.billing.boost.BoostPackage;
import com.sep.realvista.domain.billing.boost.repository.BoostPackageRepository;
import com.sep.realvista.domain.billing.boost.repository.ListingBoostRepository;
import com.sep.realvista.domain.billing.boost.repository.UserListingBoostPackageRepository;
import com.sep.realvista.domain.billing.snapshot.BoostPackageSnapshot;
import com.sep.realvista.domain.billing.snapshot.FeaturePackageSnapshot;
import com.sep.realvista.domain.billing.snapshot.SnapshotReason;
import com.sep.realvista.domain.billing.snapshot.repository.BoostPackageSnapshotRepository;
import com.sep.realvista.domain.billing.snapshot.repository.FeaturePackageSnapshotRepository;
import com.sep.realvista.domain.billing.subscription.FeaturePackage;
import com.sep.realvista.domain.billing.subscription.FeatureType;
import com.sep.realvista.domain.billing.subscription.repository.FeaturePackageRepository;
import com.sep.realvista.domain.billing.subscription.repository.UserFeatureSubscriptionRepository;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminBillingApplicationService {

    private final FeaturePackageRepository featurePackageRepository;
    private final FeaturePackageSnapshotRepository featurePackageSnapshotRepository;
    private final UserFeatureSubscriptionRepository userFeatureSubscriptionRepository;

    private final BoostPackageRepository boostPackageRepository;
    private final BoostPackageSnapshotRepository boostPackageSnapshotRepository;
    private final UserListingBoostPackageRepository userListingBoostPackageRepository;
    private final ListingBoostRepository listingBoostRepository;

    // =========================================================================
    // FEATURE PACKAGES
    // =========================================================================

    @Transactional
    public FeaturePackageAdminResponse createFeaturePackage(CreateFeaturePackageRequest request) {
        // Duplicate code check
        featurePackageRepository.findByCode(request.getCode()).ifPresent(fp -> {
            throw new BusinessConflictException(
                    "Feature package with code '" + request.getCode() + "' already exists",
                    "FEATURE_PACKAGE_CODE_DUPLICATE");
        });

        FeaturePackage featurePackage = FeaturePackage.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .featureType(FeatureType.fromDbValue(request.getFeatureType()))
                .quota(request.getQuota())
                .durationDays(request.getDurationDays())
                .price(request.getPrice())
                .isActive(true)
                .build();

        FeaturePackage saved = featurePackageRepository.save(featurePackage);
        log.info("Created FeaturePackage id={} code={}", saved.getFeaturePackageId(), saved.getCode());
        return FeaturePackageAdminResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<FeaturePackageAdminResponse> getAllFeaturePackages(boolean includeInactive) {
        List<FeaturePackage> packages = includeInactive
                ? featurePackageRepository.findAllIncludingInactive()
                : featurePackageRepository.findAllActive();
        return packages.stream().map(FeaturePackageAdminResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public FeaturePackageAdminResponse getFeaturePackageById(UUID id) {
        FeaturePackage fp = findFeaturePackageOrThrow(id);
        return FeaturePackageAdminResponse.from(fp);
    }

    @Transactional
    public FeaturePackageAdminResponse updateFeaturePackage(UUID id, UpdateFeaturePackageRequest request,
                                                             UUID adminUserId) {
        FeaturePackage fp = findFeaturePackageOrThrow(id);

        // Snapshot BEFORE update
        snapshotFeaturePackage(fp, SnapshotReason.UPDATE, adminUserId);

        fp.update(request.getName(), request.getDescription(),
                request.getQuota(), request.getDurationDays(), request.getPrice());

        FeaturePackage saved = featurePackageRepository.save(fp);
        log.info("Updated FeaturePackage id={} by admin={}", id, adminUserId);
        return FeaturePackageAdminResponse.from(saved);
    }

    @Transactional
    public FeaturePackageAdminResponse activateFeaturePackage(UUID id, UUID adminUserId) {
        FeaturePackage fp = findFeaturePackageOrThrow(id);
        fp.activate();
        FeaturePackage saved = featurePackageRepository.save(fp);
        log.info("Activated FeaturePackage id={} by admin={}", id, adminUserId);
        return FeaturePackageAdminResponse.from(saved);
    }

    @Transactional
    public FeaturePackageAdminResponse deactivateFeaturePackage(UUID id, UUID adminUserId) {
        FeaturePackage fp = findFeaturePackageOrThrow(id);

        // Snapshot BEFORE deactivate
        snapshotFeaturePackage(fp, SnapshotReason.DEACTIVATE, adminUserId);

        fp.deactivate();
        FeaturePackage saved = featurePackageRepository.save(fp);

        long activeCount = userFeatureSubscriptionRepository.countActiveByFeaturePackageId(id);
        log.info("Deactivated FeaturePackage id={} by admin={}. Active subscriptions still running: {}",
                id, adminUserId, activeCount);
        return FeaturePackageAdminResponse.from(saved);
    }

    @Transactional
    public void deleteFeaturePackage(UUID id, UUID adminUserId) {
        FeaturePackage fp = findFeaturePackageOrThrow(id);

        long activeCount = userFeatureSubscriptionRepository.countActiveByFeaturePackageId(id);
        if (activeCount > 0) {
            throw new BusinessConflictException(
                    "Cannot delete FeaturePackage '" + fp.getCode() + "': " + activeCount
                            + " active subscription(s) still exist. Deactivate the package first.",
                    "FEATURE_PACKAGE_HAS_ACTIVE_SUBSCRIPTIONS");
        }

        // Snapshot BEFORE delete
        snapshotFeaturePackage(fp, SnapshotReason.DELETE, adminUserId);

        fp.markAsDeleted();
        featurePackageRepository.save(fp);
        log.info("Soft-deleted FeaturePackage id={} by admin={}", id, adminUserId);
    }

    @Transactional(readOnly = true)
    public List<PackageSnapshotResponse> getFeaturePackageHistory(UUID id) {
        findFeaturePackageOrThrow(id); // ensure exists
        return featurePackageSnapshotRepository.findByFeaturePackageId(id)
                .stream().map(PackageSnapshotResponse::from).toList();
    }

    // =========================================================================
    // BOOST PACKAGES
    // =========================================================================

    @Transactional
    public BoostPackageAdminResponse createBoostPackage(CreateBoostPackageRequest request) {
        boostPackageRepository.findByCode(request.getCode()).ifPresent(bp -> {
            throw new BusinessConflictException(
                    "Boost package with code '" + request.getCode() + "' already exists",
                    "BOOST_PACKAGE_CODE_DUPLICATE");
        });

        BoostPackage boostPackage = BoostPackage.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .featuredQuota(request.getFeaturedQuota())
                .hotBadgeQuota(request.getHotBadgeQuota())
                .durationDays(request.getDurationDays())
                .price(request.getPrice())
                .isActive(true)
                .build();

        BoostPackage saved = boostPackageRepository.save(boostPackage);
        log.info("Created BoostPackage id={} code={}", saved.getBoostPackageId(), saved.getCode());
        return BoostPackageAdminResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<BoostPackageAdminResponse> getAllBoostPackages(boolean includeInactive) {
        List<BoostPackage> packages = includeInactive
                ? boostPackageRepository.findAllIncludingInactive()
                : boostPackageRepository.findAllActive();
        return packages.stream().map(BoostPackageAdminResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public BoostPackageAdminResponse getBoostPackageById(UUID id) {
        BoostPackage bp = findBoostPackageOrThrow(id);
        return BoostPackageAdminResponse.from(bp);
    }

    @Transactional
    public BoostPackageAdminResponse updateBoostPackage(UUID id, UpdateBoostPackageRequest request,
                                                         UUID adminUserId) {
        BoostPackage bp = findBoostPackageOrThrow(id);

        // Snapshot BEFORE update
        snapshotBoostPackage(bp, SnapshotReason.UPDATE, adminUserId);

        bp.update(request.getName(), request.getDescription(),
                request.getFeaturedQuota(), request.getHotBadgeQuota(),
                request.getDurationDays(), request.getPrice());

        BoostPackage saved = boostPackageRepository.save(bp);
        log.info("Updated BoostPackage id={} by admin={}", id, adminUserId);
        return BoostPackageAdminResponse.from(saved);
    }

    @Transactional
    public BoostPackageAdminResponse activateBoostPackage(UUID id, UUID adminUserId) {
        BoostPackage bp = findBoostPackageOrThrow(id);
        bp.activate();
        BoostPackage saved = boostPackageRepository.save(bp);
        log.info("Activated BoostPackage id={} by admin={}", id, adminUserId);
        return BoostPackageAdminResponse.from(saved);
    }

    @Transactional
    public BoostPackageAdminResponse deactivateBoostPackage(UUID id, UUID adminUserId) {
        BoostPackage bp = findBoostPackageOrThrow(id);

        // Snapshot BEFORE deactivate
        snapshotBoostPackage(bp, SnapshotReason.DEACTIVATE, adminUserId);

        bp.deactivate();
        BoostPackage saved = boostPackageRepository.save(bp);

        long activePurchases = userListingBoostPackageRepository.countActiveByBoostPackageId(id);
        long activeBoosts = listingBoostRepository.countActiveByBoostPackageId(id);
        log.info("Deactivated BoostPackage id={} by admin={}. Active purchases: {}, active boosts: {}",
                id, adminUserId, activePurchases, activeBoosts);
        return BoostPackageAdminResponse.from(saved);
    }

    @Transactional
    public void deleteBoostPackage(UUID id, UUID adminUserId) {
        BoostPackage bp = findBoostPackageOrThrow(id);

        long activePurchases = userListingBoostPackageRepository.countActiveByBoostPackageId(id);
        long activeBoosts = listingBoostRepository.countActiveByBoostPackageId(id);

        if (activePurchases > 0 || activeBoosts > 0) {
            throw new BusinessConflictException(
                    "Cannot delete BoostPackage '" + bp.getCode() + "': "
                            + activePurchases + " active purchase(s) and "
                            + activeBoosts + " active listing boost(s) still exist. "
                            + "Deactivate the package first.",
                    "BOOST_PACKAGE_HAS_ACTIVE_USAGE");
        }

        // Snapshot BEFORE delete
        snapshotBoostPackage(bp, SnapshotReason.DELETE, adminUserId);

        bp.markAsDeleted();
        boostPackageRepository.save(bp);
        log.info("Soft-deleted BoostPackage id={} by admin={}", id, adminUserId);
    }

    @Transactional(readOnly = true)
    public List<PackageSnapshotResponse> getBoostPackageHistory(UUID id) {
        findBoostPackageOrThrow(id); // ensure exists
        return boostPackageSnapshotRepository.findByBoostPackageId(id)
                .stream().map(PackageSnapshotResponse::from).toList();
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private FeaturePackage findFeaturePackageOrThrow(UUID id) {
        return featurePackageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FeaturePackage", id));
    }

    private BoostPackage findBoostPackageOrThrow(UUID id) {
        return boostPackageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BoostPackage", id));
    }

    private void snapshotFeaturePackage(FeaturePackage fp, SnapshotReason reason, UUID adminUserId) {
        FeaturePackageSnapshot snapshot = FeaturePackageSnapshot.builder()
                .featurePackageId(fp.getFeaturePackageId())
                .code(fp.getCode())
                .name(fp.getName())
                .description(fp.getDescription())
                .featureType(fp.getFeatureType() != null ? fp.getFeatureType().name() : null)
                .quota(fp.getQuota())
                .durationDays(fp.getDurationDays())
                .price(fp.getPrice())
                .isActive(fp.getIsActive())
                .snapshotReason(reason)
                .changedByUserId(adminUserId)
                .build();
        featurePackageSnapshotRepository.save(snapshot);
    }

    private void snapshotBoostPackage(BoostPackage bp, SnapshotReason reason, UUID adminUserId) {
        BoostPackageSnapshot snapshot = BoostPackageSnapshot.builder()
                .boostPackageId(bp.getBoostPackageId())
                .code(bp.getCode())
                .name(bp.getName())
                .description(bp.getDescription())
                .featuredQuota(bp.getFeaturedQuota())
                .hotBadgeQuota(bp.getHotBadgeQuota())
                .durationDays(bp.getDurationDays())
                .price(bp.getPrice())
                .isActive(bp.getIsActive())
                .snapshotReason(reason)
                .changedByUserId(adminUserId)
                .build();
        boostPackageSnapshotRepository.save(snapshot);
    }
}
