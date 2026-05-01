package com.sep.realvista.application.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeaturedPropertyDTO {
    private UUID listingId;
    private String name;
    private String type;
    private Long sold;
    private Long rented;
    private Long views;
    private String status;
    private String imageUrl;
    private String thumbnailUrl;
}
