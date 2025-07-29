package com.smsmode.booking.resource.booking;

import com.smsmode.booking.embeddable.PartyEmbeddable;
import com.smsmode.booking.enumeration.BookingStatusEnum;
import lombok.Data;

import java.util.List;

@Data
public class BookingPostResource {
    private PartyEmbeddable party;
    private String segmentId;
    private String subSegmentId;
    private BookingStatusEnum status;
    private List<BookingItemPostResource> items;
}
