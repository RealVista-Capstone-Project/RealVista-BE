package com.sep.realvista.domain.property.media;

import com.sep.realvista.domain.common.entity.BaseEntity;
import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.user.User;
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
@Table(name = "property_media", indexes = {
        @Index(name = "idx_property_media_property", columnList = "property_id"),
        @Index(name = "idx_property_media_type", columnList = "media_type"),
        @Index(name = "idx_property_media_primary", columnList = "is_primary"),
        @Index(name = "idx_property_media_deleted", columnList = "deleted")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PropertyMedia extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "property_media_id")
    private UUID propertyMediaId;

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", insertable = false, updatable = false)
    private Property property;

    @Column(name = "upload_by", nullable = false)
    private UUID uploadBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upload_by", insertable = false, updatable = false)
    private User uploader;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 20)
    private MediaType mediaType;

    @Column(name = "media_url", nullable = false, columnDefinition = "TEXT")
    private String mediaUrl;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(name = "is_primary")
    @Builder.Default
    private Boolean isPrimary = false;

    public void markAsPrimary() {
        this.isPrimary = true;
    }

    public void removePrimary() {
        this.isPrimary = false;
    }

    public void updateMediaUrl(String url) {
        if (url != null && !url.isBlank()) {
            this.mediaUrl = url;
        }
    }

    public void updateThumbnailUrl(String url) {
        this.thumbnailUrl = url;
    }

    public boolean isImage() {
        return mediaType == MediaType.IMAGE;
    }

    public boolean isVideo() {
        return mediaType == MediaType.VIDEO;
    }

    public boolean is3D() {
        return mediaType == MediaType.THREE_D;
    }
}
