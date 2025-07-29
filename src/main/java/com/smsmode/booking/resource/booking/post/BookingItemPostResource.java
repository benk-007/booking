package com.smsmode.booking.resource.booking.post;

import com.smsmode.booking.embeddable.ChildEmbeddable;
import com.smsmode.booking.embeddable.OccupancyEmbeddable;
import com.smsmode.booking.embeddable.UnitEmbeddable;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BookingItemPostResource {
    private UnitEmbeddable unit;
    private String checkinDate;
    private String checkoutDate;
    private OccupancyEmbeddable occupancy;
    private Integer quantity;
    private BigDecimal nightlyRate;
    private Integer nights;
    private List<SupplementPostResource> supplements;
}
