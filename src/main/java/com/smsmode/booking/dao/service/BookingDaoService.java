package com.smsmode.booking.dao.service;

import com.smsmode.booking.model.BookingModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

public interface BookingDaoService {
    List<String> findBookedUnitIds(LocalDate checkinDate, LocalDate checkoutDate, boolean strict);

    List<String> findBookedUnitIdsExcludingBooking(LocalDate checkinDate, LocalDate checkoutDate,
                                                   boolean strict, String excludeBookingId);

    BookingModel save(BookingModel bookingModel);

    BookingModel findOneBy(Specification<BookingModel> specification);

    List<BookingModel> findAllBy(Specification<BookingModel> specification);

    Page<BookingModel> findAllBy(Specification<BookingModel> specification, Pageable pageable);

    void delete(BookingModel bookingModel);
}