package com.smsmode.booking.dao.service;

import com.smsmode.booking.model.BookingModel;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

public interface BookingDaoService {
    List<String> findBookedUnitIds(LocalDate checkinDate, LocalDate checkoutDate, boolean strict);

    BookingModel save(BookingModel bookingModel);

    BookingModel findOneBy(Specification<BookingModel> specification);

    List<BookingModel> findAllBy(Specification<BookingModel> specification);

    void delete(BookingModel bookingModel);
}