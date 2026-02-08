package com.sep.realvista.presentation.websocket;

import com.sep.realvista.application.listing.dto.map.MapSearchRequest;
import com.sep.realvista.application.listing.dto.map.MapSearchResponse;
import com.sep.realvista.application.listing.service.MapSearchApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * WebSocket controller for real-time map-based property searches.
 * Handles bidirectional communication for dynamic map updates.
 *
 * <p>Client usage:
 * 1. Connect to /ws with JWT token
 * 2. Subscribe to /user/queue/map/updates
 * 3. Send map bounds to /app/map/search when panning/zooming
 * 4. Receive updated markers on subscribed topic
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class MapWebSocketController {

    private final MapSearchApplicationService mapSearchService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle map search requests over WebSocket.
     * Endpoint: /app/map/search
     * Response sent to: /user/queue/map/updates (user-specific queue)
     *
     * @param request map search request from client
     * @param headerAccessor message headers containing session info
     * @param principal authenticated user principal
     */
    @MessageMapping("/map/search")
    public void handleMapSearch(
            @Payload MapSearchRequest request,
            SimpMessageHeaderAccessor headerAccessor,
            Principal principal
    ) {
        String sessionId = headerAccessor.getSessionId();
        String username = principal != null ? principal.getName() : "anonymous";

        log.info("WebSocket map search - session: {}, user: {}, bounds: ({},{}) to ({},{})",
                sessionId, username,
                request.getSouthLat(), request.getWestLng(),
                request.getNorthLat(), request.getEastLng());

        try {
            // Execute search
            MapSearchResponse response = mapSearchService.searchPropertiesOnMap(request);

            log.info("WebSocket map search completed - session: {}, markers: {}, total: {}",
                    sessionId, response.getMarkers().size(), response.getTotalCount());

            // Send response to user-specific queue
            // User will receive this on /user/queue/map/updates
            if (principal != null) {
                messagingTemplate.convertAndSendToUser(
                        principal.getName(),
                        "/queue/map/updates",
                        response
                );
            } else {
                // Fallback to session-specific topic for unauthenticated users
                messagingTemplate.convertAndSend(
                        "/topic/map/" + sessionId,
                        response
                );
            }

        } catch (IllegalArgumentException e) {
            log.error("Invalid map search request over WebSocket - session: {}, error: {}",
                    sessionId, e.getMessage());

            // Send error response
            MapSearchResponse errorResponse = MapSearchResponse.builder()
                    .markers(java.util.Collections.emptyList())
                    .totalCount(0L)
                    .hasMore(false)
                    .build();

            if (principal != null) {
                messagingTemplate.convertAndSendToUser(
                        principal.getName(),
                        "/queue/map/updates",
                        errorResponse
                );
            }
        } catch (Exception e) {
            log.error("Unexpected error in WebSocket map search - session: {}", sessionId, e);
        }
    }

    /**
     * Handle subscription notifications.
     * This method would be called when client subscribes to map updates.
     *
     * @param headerAccessor message headers
     * @param principal authenticated user principal
     */
    @MessageMapping("/map/subscribe")
    public void handleSubscription(
            SimpMessageHeaderAccessor headerAccessor,
            Principal principal
    ) {
        String sessionId = headerAccessor.getSessionId();
        String username = principal != null ? principal.getName() : "anonymous";

        log.info("User subscribed to map updates - session: {}, user: {}", sessionId, username);

        // Send initial acknowledgment
        if (principal != null) {
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/map/updates",
                    MapSearchResponse.builder()
                            .markers(java.util.Collections.emptyList())
                            .totalCount(0L)
                            .hasMore(false)
                            .build()
            );
        }
    }
}
