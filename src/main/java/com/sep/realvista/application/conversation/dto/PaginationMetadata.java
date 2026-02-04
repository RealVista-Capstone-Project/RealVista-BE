package com.sep.realvista.application.conversation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pagination metadata for cursor-based pagination.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationMetadata {

    private int limit;
    private boolean hasMore;
    private String nextCursor;
    private String prevCursor;
}
