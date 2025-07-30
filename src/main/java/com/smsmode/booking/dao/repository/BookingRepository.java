package com.smsmode.booking.dao.repository;

import com.smsmode.booking.model.BookingModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Guest entity operations.
 * Provides CRUD operations and query capabilities for Guest entities.
 *
 * @author hamzahabchi (contact: hamza.habchi@messaging-technologies.com)
 * <p>Created 16 Jun 2025</p>
 */
@Repository
public interface BookingRepository extends JpaRepository<BookingModel, String>, JpaSpecificationExecutor<BookingModel> {

    @Query("""
                SELECT b.unit.unitId
                FROM BookingModel b
                WHERE b.checkinDate < :checkoutDate
                  AND b.checkoutDate > :checkinDate
                  AND b.status = 'CONFIRMED'
                  AND b.type = 'SINGLE'
            """)
    List<String> findStrictlyBookedUnitIds(@Param("checkinDate") LocalDate checkinDate,
                                           @Param("checkoutDate") LocalDate checkoutDate);

    @Query("""
                SELECT b.unit.unitId
                FROM BookingModel b
                WHERE b.checkinDate < :checkoutDate
                  AND b.checkoutDate > :checkinDate
                  AND b.status = 'CONFIRMED'
                  AND b.type = 'SINGLE'
                  AND (b.parentBooking.id != :excludeBookingId OR b.parentBooking IS NULL)
                  AND b.id != :excludeBookingId
            """)
    List<String> findStrictlyBookedUnitIdsExcludingBooking(@Param("checkinDate") LocalDate checkinDate,
                                                           @Param("checkoutDate") LocalDate checkoutDate,
                                                           @Param("excludeBookingId") String excludeBookingId);


    @Query("""
                SELECT DISTINCT b.unit.unitId 
                FROM BookingModel b 
                WHERE b.checkinDate <= :checkoutDate 
                  AND b.checkoutDate >= :checkinDate
                  AND b.status = 'CONFIRMED'
                  AND b.type = 'SINGLE'
            """)
    List<String> findLooselyBookedUnitIds(@Param("checkinDate") LocalDate checkinDate,
                                          @Param("checkoutDate") LocalDate checkoutDate);


    @Query("""
                SELECT DISTINCT b.unit.unitId 
                FROM BookingModel b 
                WHERE b.checkinDate <= :checkoutDate 
                  AND b.checkoutDate >= :checkinDate
                  AND b.status = 'CONFIRMED'
                  AND b.type = 'SINGLE'
                  AND (b.parentBooking.id != :excludeBookingId OR b.parentBooking IS NULL)
                  AND b.id != :excludeBookingId
            """)
    List<String> findLooselyBookedUnitIdsExcludingBooking(@Param("checkinDate") LocalDate checkinDate,
                                                          @Param("checkoutDate") LocalDate checkoutDate,
                                                          @Param("excludeBookingId") String excludeBookingId);
}