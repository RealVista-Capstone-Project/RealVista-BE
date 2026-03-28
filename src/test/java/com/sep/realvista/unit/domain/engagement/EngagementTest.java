package com.sep.realvista.unit.domain.engagement;

import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.engagement.Engagement;
import com.sep.realvista.domain.engagement.EngagementStatus;
import com.sep.realvista.domain.engagement.EngagementType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the Engagement domain entity.
 *
 * Tests domain business logic for state transitions: finish, cancel, accept, reject.
 */
@DisplayName("Engagement Domain Entity Tests")
class EngagementTest {

    private Engagement createEngagement(EngagementStatus status) {
        return Engagement.builder()
                .engagementId(UUID.randomUUID())
                .initiatorId(UUID.randomUUID())
                .receiverId(UUID.randomUUID())
                .engagementType(EngagementType.AGENT_PROPOSAL)
                .status(status)
                .build();
    }

    @Nested
    @DisplayName("finish()")
    class Finish {

        @Test
        @DisplayName("Should transition from ACCEPTED to FINISHED")
        void shouldFinishAcceptedEngagement() {
            // Arrange
            Engagement engagement = createEngagement(EngagementStatus.ACCEPTED);

            // Act
            engagement.finish();

            // Assert
            assertThat(engagement.getStatus()).isEqualTo(EngagementStatus.FINISHED);
        }

        @Test
        @DisplayName("Should throw when finishing SUBMITTED engagement")
        void shouldThrowWhenFinishingSubmitted() {
            Engagement engagement = createEngagement(EngagementStatus.SUBMITTED);

            assertThatThrownBy(engagement::finish)
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("Only ACCEPTED engagements can be finished");
        }

        @Test
        @DisplayName("Should throw when finishing CANCELLED engagement")
        void shouldThrowWhenFinishingCancelled() {
            Engagement engagement = createEngagement(EngagementStatus.CANCELLED);

            assertThatThrownBy(engagement::finish)
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("Only ACCEPTED");
        }

        @Test
        @DisplayName("Should throw when finishing FINISHED engagement")
        void shouldThrowWhenFinishingFinished() {
            Engagement engagement = createEngagement(EngagementStatus.FINISHED);

            assertThatThrownBy(engagement::finish)
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("Only ACCEPTED");
        }

        @Test
        @DisplayName("Should throw when finishing REJECTED engagement")
        void shouldThrowWhenFinishingRejected() {
            Engagement engagement = createEngagement(EngagementStatus.REJECTED);

            assertThatThrownBy(engagement::finish)
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("Only ACCEPTED");
        }
    }

    @Nested
    @DisplayName("cancel()")
    class Cancel {

        @Test
        @DisplayName("Should cancel ACCEPTED engagement with reason")
        void shouldCancelAcceptedWithReason() {
            Engagement engagement = createEngagement(EngagementStatus.ACCEPTED);

            engagement.cancel("Service no longer needed");

            assertThat(engagement.getStatus()).isEqualTo(EngagementStatus.CANCELLED);
            assertThat(engagement.getCancellationReason()).isEqualTo("Service no longer needed");
        }

        @Test
        @DisplayName("Should cancel SUBMITTED engagement without reason")
        void shouldCancelSubmittedWithoutReason() {
            Engagement engagement = createEngagement(EngagementStatus.SUBMITTED);

            engagement.cancel(null);

            assertThat(engagement.getStatus()).isEqualTo(EngagementStatus.CANCELLED);
            assertThat(engagement.getCancellationReason()).isNull();
        }

        @Test
        @DisplayName("Should cancel SUBMITTED engagement with reason")
        void shouldCancelSubmittedWithReason() {
            Engagement engagement = createEngagement(EngagementStatus.SUBMITTED);

            engagement.cancel("Changed my mind");

            assertThat(engagement.getStatus()).isEqualTo(EngagementStatus.CANCELLED);
            assertThat(engagement.getCancellationReason()).isEqualTo("Changed my mind");
        }

        @Test
        @DisplayName("Should throw when cancelling ACCEPTED without reason")
        void shouldThrowWhenCancellingAcceptedWithoutReason() {
            Engagement engagement = createEngagement(EngagementStatus.ACCEPTED);

            assertThatThrownBy(() -> engagement.cancel(null))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("Cancellation reason is required");
        }

        @Test
        @DisplayName("Should throw when cancelling ACCEPTED with blank reason")
        void shouldThrowWhenCancellingAcceptedWithBlankReason() {
            Engagement engagement = createEngagement(EngagementStatus.ACCEPTED);

            assertThatThrownBy(() -> engagement.cancel("   "))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("Cancellation reason is required");
        }

        @Test
        @DisplayName("Should throw when cancelling FINISHED engagement")
        void shouldThrowWhenCancellingFinished() {
            Engagement engagement = createEngagement(EngagementStatus.FINISHED);

            assertThatThrownBy(() -> engagement.cancel("reason"))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("ACCEPTED or SUBMITTED");
        }

        @Test
        @DisplayName("Should throw when cancelling REJECTED engagement")
        void shouldThrowWhenCancellingRejected() {
            Engagement engagement = createEngagement(EngagementStatus.REJECTED);

            assertThatThrownBy(() -> engagement.cancel("reason"))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("ACCEPTED or SUBMITTED");
        }

        @Test
        @DisplayName("Should throw when cancelling already CANCELLED engagement")
        void shouldThrowWhenCancellingAlreadyCancelled() {
            Engagement engagement = createEngagement(EngagementStatus.CANCELLED);

            assertThatThrownBy(() -> engagement.cancel("reason"))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("ACCEPTED or SUBMITTED");
        }
    }

    @Nested
    @DisplayName("accept()")
    class Accept {

        @Test
        @DisplayName("Should transition to ACCEPTED status")
        void shouldAccept() {
            Engagement engagement = createEngagement(EngagementStatus.SUBMITTED);

            engagement.accept();

            assertThat(engagement.getStatus()).isEqualTo(EngagementStatus.ACCEPTED);
        }
    }

    @Nested
    @DisplayName("reject()")
    class Reject {

        @Test
        @DisplayName("Should transition to REJECTED status")
        void shouldReject() {
            Engagement engagement = createEngagement(EngagementStatus.SUBMITTED);

            engagement.reject();

            assertThat(engagement.getStatus()).isEqualTo(EngagementStatus.REJECTED);
        }
    }

    @Nested
    @DisplayName("Builder defaults")
    class BuilderDefaults {

        @Test
        @DisplayName("Should default status to SUBMITTED")
        void shouldDefaultToSubmitted() {
            Engagement engagement = Engagement.builder()
                    .engagementId(UUID.randomUUID())
                    .initiatorId(UUID.randomUUID())
                    .receiverId(UUID.randomUUID())
                    .engagementType(EngagementType.AGENT_PROPOSAL)
                    .build();

            assertThat(engagement.getStatus()).isEqualTo(EngagementStatus.SUBMITTED);
        }
    }
}
