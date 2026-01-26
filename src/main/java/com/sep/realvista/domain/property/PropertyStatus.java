package com.sep.realvista.domain.property;

/**
 * Property verification and availability status.
 * 
 * <p>Status flow:
 * <pre>
 * DRAFT -> PENDING -> VERIFIED/REJECTED
 *                         |
 *                    AVAILABLE -> RESERVED -> SOLD
 * </pre>
 * 
 * <ul>
 *   <li>DRAFT: Initial state, property being created/edited</li>
 *   <li>PENDING: Submitted for verification by admin/verifier</li>
 *   <li>VERIFIED: Passed verification, ready to be listed</li>
 *   <li>REJECTED: Failed verification, needs correction</li>
 *   <li>AVAILABLE: Active and can be listed for sale/rent</li>
 *   <li>RESERVED: Someone has expressed interest/deposit</li>
 *   <li>SOLD: Property has been sold or rented</li>
 * </ul>
 */
public enum PropertyStatus {
    DRAFT,
    PENDING,
    VERIFIED,
    REJECTED,
    AVAILABLE,
    RESERVED,
    SOLD
}
