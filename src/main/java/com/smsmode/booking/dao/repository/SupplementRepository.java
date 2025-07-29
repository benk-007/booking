package com.smsmode.booking.dao.repository;

import com.smsmode.booking.model.SupplementModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplementRepository extends JpaRepository<SupplementModel, String> {
    List<SupplementModel> findByBookingId(String bookingId);
}
