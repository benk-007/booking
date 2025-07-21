package com.smsmode.booking.dao.service;

import com.smsmode.booking.model.BookingModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface BookingDaoService {
    BookingModel save(BookingModel bookingModel);

    Page<BookingModel> findAllBy(Specification<BookingModel> specification, Pageable pageable);

    BookingModel findOneBy(Specification<BookingModel> specification);

    boolean existsById(String guestId);

    void deleteById(String guestId);

    void delete(BookingModel guest);

}