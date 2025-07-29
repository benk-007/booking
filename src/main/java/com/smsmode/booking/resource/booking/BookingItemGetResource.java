package com.smsmode.booking.resource.booking;

import com.smsmode.booking.embeddable.ChildEmbeddable;
import com.smsmode.booking.embeddable.UnitEmbeddable;
import com.smsmode.booking.resource.common.SupplementPostResource;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class BookingItemGetResource {
    private String id;
    private LocalDate checkinDate;
    private LocalDate checkoutDate;
    private UnitEmbeddable unit;
    private Integer quantity;
    private Integer adults;
    private List<ChildEmbeddable> children;
    private BigDecimal nightlyRate;
    private Integer nights;
    private BigDecimal total;
    private List<SupplementPostResource> supplements;
}