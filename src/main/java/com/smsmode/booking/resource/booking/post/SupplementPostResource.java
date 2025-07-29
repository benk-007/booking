package com.smsmode.booking.resource.booking.post;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Resource representing a supplement/fee in a booking item
 */
@Data
public class SupplementPostResource {
    private String label;
    private String description;
    private BigDecimal price;
}