package com.sep.realvista.domain.property;

/**
 * Severity level returned by the duplicate-address check.
 *
 * <ul>
 *   <li>NONE        – no match found, allow creation freely</li>
 *   <li>INFO        – nearby coordinate or inactive address from a different owner; just informational</li>
 *   <li>SOFT_WARNING – active address conflict or same-owner inactive match; FE must show modal and
 *                     collect an override reason before allowing submission</li>
 *   <li>HARD_BLOCK  – same owner + active property at exact address; creation must be blocked</li>
 * </ul>
 */
public enum DuplicateSeverity {
    NONE,
    INFO,
    SOFT_WARNING,
    HARD_BLOCK
}
