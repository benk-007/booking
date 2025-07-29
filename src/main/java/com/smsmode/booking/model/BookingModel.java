/**
 * <p>Copyright (C) Calade Technologies, Inc - All Rights Reserved Unauthorized copying of this
 * file, via any medium is strictly prohibited Proprietary and confidential
 */
package com.smsmode.booking.model;

import com.smsmode.booking.embeddable.ChildEmbeddable;
import com.smsmode.booking.embeddable.OccupancyEmbeddable;
import com.smsmode.booking.embeddable.PartyEmbeddable;
import com.smsmode.booking.embeddable.UnitEmbeddable;
import com.smsmode.booking.enumeration.BookingStatusEnum;
import com.smsmode.booking.enumeration.BookingTypeEnum;
import com.smsmode.booking.enumeration.PaymentMethodEnum;
import com.smsmode.booking.model.base.AbstractBaseModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a Guest in the PMS system.
 * A guest is a person who makes a reservation at the hotel.
 *
 * @author hamzahabchi (contact: hamza.habchi@messaging-technologies.com)
 * <p>Created 16 Jun 2025</p>
 */
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "X_BOOKING")
public class BookingModel extends AbstractBaseModel {

    private LocalDate checkinDate;
    private LocalDate checkoutDate;
    @Embedded
    private UnitEmbeddable unit;
    @Embedded
    private PartyEmbeddable party;

    private String segmentId;
    private String subSegmentId;

    @Enumerated(EnumType.STRING)
    private BookingStatusEnum status;

    @Enumerated(EnumType.STRING)
    private BookingTypeEnum type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_BOOKING_ID")
    private BookingModel parentBooking;

    private String guestName;

    @Enumerated(EnumType.STRING)
    private PaymentMethodEnum paymentMethod;

    private BigDecimal guaranteeAmount;

    @Column(columnDefinition = "TEXT")
    private String specialNotes;

    private Integer quantity;

    @Embedded
    private OccupancyEmbeddable occupancy;

    private BigDecimal nightlyRate;
    private Integer nights;
    private BigDecimal total;

//    @OneToMany(fetch = FetchType.LAZY)
//    private List<SupplementModel> supplements = new ArrayList<>();
}