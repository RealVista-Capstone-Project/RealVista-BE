package com.sep.realvista.domain.user.preference;

import com.sep.realvista.domain.common.entity.BaseEntity;
import com.sep.realvista.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "setting_preferences")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString(exclude = {"user"})
public class SettingPreference extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "setting_preference_id")
    private UUID settingPreferenceId;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "in_app_enabled")
    @Builder.Default
    private Boolean inAppEnabled = true;

    @Column(name = "email_enabled")
    @Builder.Default
    private Boolean emailEnabled = true;

    @Column(name = "push_enabled")
    @Builder.Default
    private Boolean pushEnabled = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "event_preference", columnDefinition = "json")
    private String eventPreference;

    @Column(name = "contact_via_email")
    @Builder.Default
    private Boolean contactViaEmail = true;

    @Column(name = "contact_via_phone")
    @Builder.Default
    private Boolean contactViaPhone = false;

    @Column(name = "hide_phone_number")
    @Builder.Default
    private Boolean hidePhoneNumber = true;

    @Column(name = "hide_email")
    @Builder.Default
    private Boolean hideEmail = true;

    public void enableInApp() {
        this.inAppEnabled = true;
    }

    public void disableInApp() {
        this.inAppEnabled = false;
    }

    public void enableEmail() {
        this.emailEnabled = true;
    }

    public void disableEmail() {
        this.emailEnabled = false;
    }

    public void enablePush() {
        this.pushEnabled = true;
    }

    public void disablePush() {
        this.pushEnabled = false;
    }
}
