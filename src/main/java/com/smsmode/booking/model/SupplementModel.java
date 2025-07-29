package com.smsmode.booking.model;

import com.smsmode.booking.model.base.AbstractBaseModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entity representing a Supplement in a booking item.
 */
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "X_BOOKING_SUPPLEMENT")
public class SupplementModel extends AbstractBaseModel {

    private String label;
    private String description;
    private BigDecimal price;
    private String bookingId;
}