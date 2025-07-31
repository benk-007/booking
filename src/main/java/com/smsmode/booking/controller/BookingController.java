/**
 * <p>Copyright (C) Calade Technologies, Inc - All Rights Reserved Unauthorized copying of this
 * file, via any medium is strictly prohibited Proprietary and confidential
 */
package com.smsmode.booking.controller;

import com.smsmode.booking.resource.booking.get.BookingGetResource;
import com.smsmode.booking.resource.booking.patch.BookingPatchResource;
import com.smsmode.booking.resource.booking.post.BookingItemPostResource;
import com.smsmode.booking.resource.booking.post.BookingPostResource;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * TODO: add your documentation
 *
 * @author hamzahabchi (contact: hamza.habchi@messaging-technologies.com)
 * <p>Created 07 Jul 2025</p>
 */

@RequestMapping("/bookings")
public interface BookingController {
    @PostMapping
    ResponseEntity<BookingGetResource> createBooking(@RequestBody @Valid BookingPostResource bookingPostResource);

    @PostMapping("/{bookingId}/items")
    ResponseEntity<BookingGetResource> addItemToBooking(
            @PathVariable("bookingId") String bookingId,
            @RequestBody @Valid BookingItemPostResource bookingItemPostResource);

    @GetMapping("/{bookingId}")
    ResponseEntity<BookingGetResource> getBookingById(@PathVariable("bookingId") String bookingId);

    @GetMapping("/drafts")
    ResponseEntity<Page<BookingGetResource>> getDraftGroupBookings(Pageable pageable);

    @DeleteMapping("/{bookingId}")
    ResponseEntity<Void> deleteBooking(@PathVariable("bookingId") String bookingId);

    @PatchMapping("/{bookingId}")
    ResponseEntity<BookingGetResource> updateBooking(
            @PathVariable("bookingId") String bookingId,
            @RequestBody @Valid BookingPatchResource bookingPatchResource);
}

