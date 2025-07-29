package com.smsmode.booking.resource.booking;

import com.smsmode.booking.embeddable.ChildEmbeddable;
import com.smsmode.booking.embeddable.UnitEmbeddable;
import com.smsmode.booking.resource.common.SupplementPostResource;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BookingItemPostResource {
    private UnitEmbeddable unit;
    private String checkinDate;
    private String checkoutDate;
    private Integer adults;
    private List<ChildEmbeddable> children;
    private Integer quantity;
    private BigDecimal nightlyRate;
    private Integer nights;
    private List<SupplementPostResource> supplements;
}
