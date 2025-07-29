package com.smsmode.booking.resource.booking.get;

import com.smsmode.booking.embeddable.ChildEmbeddable;
import com.smsmode.booking.embeddable.OccupancyEmbeddable;
import com.smsmode.booking.embeddable.UnitEmbeddable;
import com.smsmode.booking.resource.booking.post.SupplementPostResource;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class  BookingItemGetResource {
    private String id;
    private LocalDate checkinDate;
    private LocalDate checkoutDate;
    private UnitEmbeddable unit;
    private Integer quantity;
    private OccupancyEmbeddable occupancy;
    private BigDecimal nightlyRate;
    private Integer nights;
    private BigDecimal total;
    private List<SupplementPostResource> supplements;
}