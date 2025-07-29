package com.smsmode.booking.service.impl;

import com.smsmode.booking.dao.service.BookingDaoService;
import com.smsmode.booking.dao.service.SupplementDaoService;
import com.smsmode.booking.dao.specification.BookingSpecification;
import com.smsmode.booking.enumeration.BookingStatusEnum;
import com.smsmode.booking.enumeration.BookingTypeEnum;
import com.smsmode.booking.mapper.BookingMapper;
import com.smsmode.booking.model.BookingModel;
import com.smsmode.booking.model.SupplementModel;
import com.smsmode.booking.resource.booking.BookingGetResource;
import com.smsmode.booking.resource.booking.BookingItemGetResource;
import com.smsmode.booking.resource.booking.BookingItemPostResource;
import com.smsmode.booking.resource.booking.BookingPostResource;
import com.smsmode.booking.resource.common.SupplementPostResource;
import com.smsmode.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingDaoService bookingDaoService;
    private final BookingMapper bookingMapper;
    private final SupplementDaoService supplementDaoService;

    @Override
    public ResponseEntity<List<String>> retrieveBookedUnitIds(LocalDate checkinDate, LocalDate checkoutDate, boolean strict) {
        return ResponseEntity.ok(bookingDaoService.findBookedUnitIds(checkinDate, checkoutDate, strict));
    }

    @Override
    @Transactional
    public ResponseEntity<BookingGetResource> create(BookingPostResource bookingPostResource) {
        log.debug("Creating new booking with {} items", bookingPostResource.getItems().size());

        // Create GROUP booking
        BookingModel groupBooking = bookingMapper.postResourceToModel(bookingPostResource);
        groupBooking.setType(BookingTypeEnum.GROUP);
        groupBooking.setStatus(BookingStatusEnum.DRAFT);
        groupBooking = bookingDaoService.save(groupBooking);
        log.info("GROUP booking created with ID: {}", groupBooking.getId());

        // Create SINGLE bookings for each item
        List<BookingModel> singleBookings = new ArrayList<>();
        for (BookingItemPostResource item : bookingPostResource.getItems()) {
            BookingModel singleBooking = createSingleBooking(item, groupBooking);
            singleBookings.add(singleBooking);
        }

        // Build response
        BookingGetResource response = bookingMapper.modelToGetResource(groupBooking);
        List<BookingItemGetResource> itemResources = new ArrayList<>();

        for (BookingModel singleBooking : singleBookings) {
            BookingItemGetResource itemResource = bookingMapper.modelToItemGetResource(singleBooking);
            itemResources.add(itemResource);
        }

        response.setItems(itemResources);

        return ResponseEntity.created(URI.create("")).body(response);
    }

    @Override
    @Transactional
    public ResponseEntity<BookingGetResource> addItem(String bookingId, BookingItemPostResource bookingItemPostResource) {
        log.debug("Adding item to booking: {}", bookingId);

        BookingModel groupBooking = bookingDaoService.findOneBy(
                BookingSpecification.withIdEqual(bookingId).and(BookingSpecification.withType(BookingTypeEnum.GROUP)));

        BookingModel singleBooking = createSingleBooking(bookingItemPostResource, groupBooking);

        return retrieveById(bookingId);
    }

    @Override
    public ResponseEntity<BookingGetResource> retrieveById(String bookingId) {
        log.debug("Retrieving booking: {}", bookingId);

        BookingModel groupBooking = bookingDaoService.findOneBy(
                BookingSpecification.withIdEqual(bookingId).and(BookingSpecification.withType(BookingTypeEnum.GROUP)));

        List<BookingModel> singleBookings = bookingDaoService.findAllBy(
                BookingSpecification.withParentBooking(groupBooking).and(BookingSpecification.withType(BookingTypeEnum.SINGLE)));

        BookingGetResource response = bookingMapper.modelToGetResource(groupBooking);
        List<BookingItemGetResource> itemResources = new ArrayList<>();

        for (BookingModel singleBooking : singleBookings) {
            BookingItemGetResource itemResource = bookingMapper.modelToItemGetResource(singleBooking);
            List<SupplementModel> supplements = supplementDaoService.findByBookingId(singleBooking.getId());
            itemResources.add(itemResource);
        }

        response.setItems(itemResources);

        return ResponseEntity.ok(response);
    }

    private BookingModel createSingleBooking(BookingItemPostResource item, BookingModel groupBooking) {
        BookingModel singleBooking = bookingMapper.itemPostResourceToModel(item);
        singleBooking.setType(BookingTypeEnum.SINGLE);
        singleBooking.setStatus(BookingStatusEnum.DRAFT);
        singleBooking.setParentBooking(groupBooking);
        singleBooking.setParty(groupBooking.getParty());
        singleBooking.setSegmentId(groupBooking.getSegmentId());
        singleBooking.setSubSegmentId(groupBooking.getSubSegmentId());

        // Calculate total: accommodation + selected supplements
        BigDecimal accommodationTotal = item.getNightlyRate().multiply(BigDecimal.valueOf(item.getNights()));
        BigDecimal supplementsTotal = BigDecimal.ZERO;

        if (!CollectionUtils.isEmpty(item.getSupplements())) {
            for (SupplementPostResource supplement : item.getSupplements()) {
                BigDecimal supplementTotal = supplement.getPrice().multiply(BigDecimal.valueOf(item.getNights()));
                supplementsTotal = supplementsTotal.add(supplementTotal);
            }
        }

        singleBooking.setTotal(accommodationTotal.add(supplementsTotal));
        singleBooking = bookingDaoService.save(singleBooking);


        // Save supplements
        if (!CollectionUtils.isEmpty(item.getSupplements())) {
            for (SupplementPostResource supplementPost : item.getSupplements()) {
                SupplementModel supplement = new SupplementModel();
                supplement.setLabel(supplementPost.getLabel());
                supplement.setDescription(supplementPost.getDescription());
                supplement.setPrice(supplementPost.getPrice());
                supplement.setBookingId(singleBooking.getId());
                supplementDaoService.save(supplement);
            }
        }

        log.info("SINGLE booking created with ID: {}", singleBooking.getId());
        return singleBooking;
    }
}