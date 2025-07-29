package com.smsmode.booking.resource.booking.post;

import com.smsmode.booking.embeddable.PartyEmbeddable;
import com.smsmode.booking.enumeration.BookingStatusEnum;
import com.smsmode.booking.enumeration.PaymentMethodEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BookingPostResource {
    private PartyEmbeddable party;
    private String segmentId;
    private String subSegmentId;
    private BookingStatusEnum status;
    private String guestName;
    private PaymentMethodEnum paymentMethod;
    private BigDecimal guaranteeAmount;
    private String specialNotes;
    private List<BookingItemPostResource> items;
}
