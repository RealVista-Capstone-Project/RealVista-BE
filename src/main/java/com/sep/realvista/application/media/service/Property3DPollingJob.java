package com.sep.realvista.application.media.service;

import com.sep.realvista.domain.property.Property3DGeneration;
import com.sep.realvista.domain.property.Property3DGenerationStatus;
import com.sep.realvista.domain.property.repository.Property3DGenerationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class Property3DPollingJob {

    private final Property3DGenerationRepository generationRepository;
    private final Property3DGenerationService generationService;

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void pollPendingGenerations() {
        // In a real multi-node app, better to use shedding/lock to avoid double polling
        List<Property3DGeneration> pendingList = 
                generationRepository.findByStatus(Property3DGenerationStatus.PENDING);

        if (pendingList.isEmpty()) {
            return;
        }

        log.info("Polling Marble API for {} pending 3D generations", pendingList.size());
        for (Property3DGeneration gen : pendingList) {
            try {
                generationService.checkAndUpdateStatus(gen);
            } catch (Exception e) {
                log.error("Failed to poll generation {}: {}", gen.getId(), e.getMessage());
            }
        }
    }
}
