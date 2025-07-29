package com.smsmode.booking.resource.booking.get;

import com.smsmode.booking.embeddable.PartyEmbeddable;
import com.smsmode.booking.enumeration.BookingStatusEnum;
import com.smsmode.booking.enumeration.BookingTypeEnum;
import com.smsmode.booking.enumeration.PaymentMethodEnum;
import com.smsmode.booking.resource.common.AuditGetResource;
import lombok.Data;

import java.math.BigDecimal;
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
    private String guestName;
    private PaymentMethodEnum paymentMethod;
    private BigDecimal guaranteeAmount;
    private String specialNotes;
    private String parentBookingId;
    private List<BookingItemGetResource> items;
    private AuditGetResource audit;
}
