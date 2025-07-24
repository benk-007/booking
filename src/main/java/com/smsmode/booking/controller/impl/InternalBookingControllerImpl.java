/**
 * <p>Copyright (C) Calade Technologies, Inc - All Rights Reserved Unauthorized copying of this
 * file, via any medium is strictly prohibited Proprietary and confidential
 */
package com.smsmode.booking.controller.impl;

import com.smsmode.booking.controller.InternalBookingController;
import com.smsmode.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * TODO: add your documentation
 *
 * @author hamzahabchi (contact: hamza.habchi@messaging-technologies.com)
 * <p>Created 24 Jul 2025</p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class InternalBookingControllerImpl implements InternalBookingController {

    private final BookingService bookingService;

    @Override
    public ResponseEntity<List<String>> getBookedUnits(LocalDate checkinDate, LocalDate checkoutDate, boolean strict) {
        return bookingService.retrieveBookedUnitIds(checkinDate, checkoutDate, strict);
    }
}
