package com.smsmode.booking.resource.booking;

import com.smsmode.booking.embeddable.PartyEmbeddable;
import com.smsmode.booking.enumeration.BookingStatusEnum;
import com.smsmode.booking.enumeration.BookingTypeEnum;
import com.smsmode.booking.resource.common.AuditGetResource;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BookingGetResource {
    private String id;
    private LocalDate checkinDate;
    private LocalDate checkoutDate;
    private PartyEmbeddable party;
    private String segmentId;
    private String subSegmentId;
    private BookingStatusEnum status;
    private BookingTypeEnum type;
    private String parentBookingId;
    private List<BookingItemGetResource> items;
    private AuditGetResource audit;
}
