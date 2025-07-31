package com.smsmode.booking.resource.booking.patch;

import com.smsmode.booking.enumeration.BookingStatusEnum;
import com.smsmode.booking.enumeration.PaymentMethodEnum;
import com.smsmode.booking.resource.booking.post.SupplementPostResource;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resource for updating booking information (status confirmation or item updates)
 */
@Data
public class BookingPatchResource {
    // For status confirmation
    private BookingStatusEnum status;

    // For booking info updates
    private String guestName;
    private PaymentMethodEnum paymentMethod;
    private BigDecimal guaranteeAmount;
    private String specialNotes;
    private String mobile;
    private String email;

    // For item updates
    private BigDecimal nightlyRate;
    private List<SupplementPostResource> supplements;
}