package com.smsmode.booking.service.impl;

import com.smsmode.booking.dao.service.BookingDaoService;
import com.smsmode.booking.dao.service.SupplementDaoService;
import com.smsmode.booking.dao.specification.BookingSpecification;
import com.smsmode.booking.dao.specification.SupplementSpecification;
import com.smsmode.booking.embeddable.ChildEmbeddable;
import com.smsmode.booking.enumeration.BookingStatusEnum;
import com.smsmode.booking.enumeration.BookingTypeEnum;
import com.smsmode.booking.exception.ConflictException;
import com.smsmode.booking.exception.enumeration.ConflictExceptionTitleEnum;
import com.smsmode.booking.mapper.BookingMapper;
import com.smsmode.booking.model.BookingModel;
import com.smsmode.booking.model.SupplementModel;
import com.smsmode.booking.resource.booking.get.BookingGetResource;
import com.smsmode.booking.resource.booking.get.BookingItemGetResource;
import com.smsmode.booking.resource.booking.patch.BookingPatchResource;
import com.smsmode.booking.resource.booking.post.BookingItemPostResource;
import com.smsmode.booking.resource.booking.post.BookingPostResource;
import com.smsmode.booking.resource.booking.post.SupplementPostResource;
import com.smsmode.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingDaoService bookingDaoService;
    private final BookingMapper bookingMapper;
    private final SupplementDaoService supplementDaoService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public ResponseEntity<List<String>> retrieveBookedUnitIds(LocalDate checkinDate, LocalDate checkoutDate, boolean strict) {
        return ResponseEntity.ok(bookingDaoService.findBookedUnitIds(checkinDate, checkoutDate, strict));
    }

    @Override
    @Transactional
    public ResponseEntity<BookingGetResource> create(BookingPostResource bookingPostResource) {
        log.debug("Creating new booking with {} items", bookingPostResource.getItems().size());

        BookingModel groupBooking = bookingMapper.postResourceToModel(bookingPostResource);
        groupBooking.setType(BookingTypeEnum.GROUP);
        groupBooking.setStatus(BookingStatusEnum.DRAFT);
        groupBooking = bookingDaoService.save(groupBooking);
        log.info("GROUP booking created with ID: {}", groupBooking.getId());

        List<BookingModel> singleBookings = new ArrayList<>();
        for (BookingItemPostResource item : bookingPostResource.getItems()) {
            BookingModel singleBooking = createSingleBooking(item, groupBooking);
            singleBookings.add(singleBooking);
        }

        BookingGetResource response = buildBookingResponse(groupBooking, singleBookings);
        return ResponseEntity.created(URI.create("")).body(response);
    }

    @Override
    @Transactional
    public ResponseEntity<BookingGetResource> addItem(String bookingId, BookingItemPostResource bookingItemPostResource) {
        log.debug("Adding item to booking: {}", bookingId);

        BookingModel booking = bookingDaoService.findOneBy(BookingSpecification.withIdEqual(bookingId));
        BookingModel groupBooking = booking.getType() == BookingTypeEnum.GROUP ?
                booking : booking.getParentBooking();
        BookingModel existingItem = findExistingItem(groupBooking, bookingItemPostResource);

        if (existingItem != null) {
            if (bookingItemPostResource.getQuantity() == 0) {
                List<BookingModel> remainingItems = findSingleBookings(groupBooking);
                boolean isLastItem = remainingItems.size() == 1;

                handleExistingItem(existingItem, bookingItemPostResource);

                if (isLastItem) {
                    log.info("Last item deleted, GROUP booking also deleted");
                    return ResponseEntity.noContent().build();
                }
            } else {
                handleExistingItem(existingItem, bookingItemPostResource);
            }
        } else {
            createSingleBooking(bookingItemPostResource, groupBooking);
            log.info("Created new item");
        }

        return retrieveById(bookingId);
    }

    @Override
    public ResponseEntity<BookingGetResource> retrieveById(String bookingId) {
        log.debug("Retrieving booking: {}", bookingId);

        BookingModel booking = bookingDaoService.findOneBy(BookingSpecification.withIdEqual(bookingId));

        if (booking.getType() == BookingTypeEnum.GROUP) {
            List<BookingModel> singleBookings = findSingleBookings(booking);
            return ResponseEntity.ok(buildBookingResponse(booking, singleBookings));
        } else {
            BookingGetResource response = bookingMapper.modelToGetResource(booking);
            response.setItems(List.of(bookingMapper.modelToItemGetResource(booking)));
            return ResponseEntity.ok(response);
        }
    }


    @Override
    public ResponseEntity<Page<BookingGetResource>> retrieveDraftGroupBookings(Pageable pageable) {
        log.debug("Retrieving draft GROUP bookings with pagination");

        Specification<BookingModel> specification = Specification.where(
                BookingSpecification.withStatus(BookingStatusEnum.DRAFT)
                        .and(BookingSpecification.withType(BookingTypeEnum.GROUP))
        );

        Page<BookingModel> bookingsPage = bookingDaoService.findAllBy(specification, pageable);
        Page<BookingGetResource> responsePage = bookingsPage.map(booking -> {
            List<BookingModel> singleBookings = findSingleBookings(booking);
            return buildBookingResponse(booking, singleBookings);
        });

        log.info("Retrieved {} draft GROUP bookings", responsePage.getTotalElements());
        return ResponseEntity.ok(responsePage);
    }

    @Override
    @Transactional
    public ResponseEntity<Void> deleteById(String bookingId) {
        log.debug("Deleting booking: {}", bookingId);

        BookingModel booking = bookingDaoService.findOneBy(BookingSpecification.withIdEqual(bookingId));

        if (booking.getType() == BookingTypeEnum.GROUP) {
            log.debug("Deleting GROUP booking and all its items");
            deleteGroupBooking(booking);
        } else {
            log.debug("Deleting SINGLE booking item");
            deleteSingleBooking(booking);
        }

        return ResponseEntity.noContent().build();
    }

    @Override
    @Transactional
    public ResponseEntity<BookingGetResource> updateBooking(String bookingId, BookingPatchResource bookingPatchResource) {
        log.debug("Updating booking: {}", bookingId);

        BookingModel booking = bookingDaoService.findOneBy(BookingSpecification.withIdEqual(bookingId));

        if (bookingPatchResource.getStatus() != null) {
            return handleStatusConfirmation(booking, bookingPatchResource);
        } else {
            return handleBookingInfoUpdate(booking, bookingPatchResource);
        }
    }

    private List<BookingModel> findSingleBookings(BookingModel groupBooking) {
        return bookingDaoService.findAllBy(
                BookingSpecification.withParentBooking(groupBooking).and(BookingSpecification.withType(BookingTypeEnum.SINGLE)));
    }

    private BookingGetResource buildBookingResponse(BookingModel groupBooking, List<BookingModel> singleBookings) {
        BookingGetResource response = bookingMapper.modelToGetResource(groupBooking);
        List<BookingItemGetResource> itemResources = new ArrayList<>();

        for (BookingModel singleBooking : singleBookings) {
            BookingItemGetResource itemResource = bookingMapper.modelToItemGetResource(singleBooking);

            List<SupplementModel> supplements = supplementDaoService.findAllBy(
                    SupplementSpecification.withBookingId(singleBooking.getId()));
            itemResource.setSupplements(bookingMapper.supplementModelsToPostResources(supplements));

            itemResources.add(itemResource);
        }

        response.setItems(itemResources);
        return response;
    }

    private void handleExistingItem(BookingModel existingItem, BookingItemPostResource itemRequest) {
        int finalQuantity = itemRequest.getQuantity();

        if (finalQuantity == 0) {
            log.debug("Quantity is 0, deleting item: {}", existingItem.getId());
            deleteItemAndCheckParent(existingItem);
        } else {
            log.debug("Updating item quantity to: {}", finalQuantity);
            updateItemQuantity(existingItem, itemRequest, finalQuantity);
        }
    }

    private void deleteItemAndCheckParent(BookingModel existingItem) {
        BookingModel groupBooking = existingItem.getParentBooking();

        supplementDaoService.deleteBy(SupplementSpecification.withBookingId(existingItem.getId()));
        bookingDaoService.delete(existingItem);

        // Check if this was the last item
        List<BookingModel> remainingItems = findSingleBookings(groupBooking);
        if (remainingItems.isEmpty()) {
            bookingDaoService.delete(groupBooking);
            log.info("Deleted last item and GROUP booking");
        } else {
            log.info("Deleted item, {} items remaining", remainingItems.size());
        }
    }

    private void updateItemQuantity(BookingModel existingItem, BookingItemPostResource itemRequest, int finalQuantity) {
        existingItem.setQuantity(finalQuantity);

        BigDecimal accommodationTotal = itemRequest.getNightlyRate()
                .multiply(BigDecimal.valueOf(itemRequest.getNights()))
                .multiply(BigDecimal.valueOf(finalQuantity));

        BigDecimal supplementsTotal = calculateSupplementsTotal(itemRequest, finalQuantity);
        existingItem.setTotal(accommodationTotal.add(supplementsTotal));

        bookingDaoService.save(existingItem);
        log.info("Updated item quantity to: {}", finalQuantity);
    }

    private BookingModel findExistingItem(BookingModel groupBooking, BookingItemPostResource newItem) {
        List<BookingModel> existingItems = findSingleBookings(groupBooking);
        return existingItems.stream()
                .filter(item -> isSameItem(item, newItem))
                .findFirst()
                .orElse(null);
    }

    private boolean isSameItem(BookingModel existing, BookingItemPostResource newItem) {
        return isSameUnit(existing, newItem) &&
                isSameDates(existing, newItem) &&
                isSameOccupancy(existing, newItem) &&
                isSameRates(existing, newItem) &&
                isSameSupplements(existing.getId(), newItem.getSupplements());
    }

    private boolean isSameUnit(BookingModel existing, BookingItemPostResource newItem) {
        return existing.getUnit().getUnitId().equals(newItem.getUnit().getUnitId());
    }

    private boolean isSameDates(BookingModel existing, BookingItemPostResource newItem) {
        LocalDate newCheckinDate = LocalDate.parse(newItem.getCheckinDate(), DATE_FORMATTER);
        LocalDate newCheckoutDate = LocalDate.parse(newItem.getCheckoutDate(), DATE_FORMATTER);

        return existing.getCheckinDate().equals(newCheckinDate) &&
                existing.getCheckoutDate().equals(newCheckoutDate);
    }

    private boolean isSameOccupancy(BookingModel existing, BookingItemPostResource newItem) {
        return existing.getOccupancy().getAdults().equals(newItem.getOccupancy().getAdults()) &&
                isSameChildren(existing.getOccupancy().getChildren(), newItem.getOccupancy().getChildren());
    }

    private boolean isSameRates(BookingModel existing, BookingItemPostResource newItem) {
        return existing.getNightlyRate().compareTo(newItem.getNightlyRate()) == 0 &&
                existing.getNights().equals(newItem.getNights());
    }

    private boolean isSameChildren(List<ChildEmbeddable> existing,
                                   List<ChildEmbeddable> newChildren) {
        if (existing.size() != newChildren.size()) {
            return false;
        }

        List<ChildEmbeddable> sortedExisting = existing.stream()
                .sorted((a, b) -> a.getAge().compareTo(b.getAge()))
                .collect(Collectors.toList());

        List<ChildEmbeddable> sortedNew = newChildren.stream()
                .sorted((a, b) -> a.getAge().compareTo(b.getAge()))
                .collect(Collectors.toList());

        for (int i = 0; i < sortedExisting.size(); i++) {
            if (!sortedExisting.get(i).getAge().equals(sortedNew.get(i).getAge()) ||
                    !sortedExisting.get(i).getQuantity().equals(sortedNew.get(i).getQuantity())) {
                return false;
            }
        }

        return true;
    }

    private boolean isSameSupplements(String bookingId, List<SupplementPostResource> newSupplements) {
        List<SupplementModel> existingSupplements = supplementDaoService.findAllBy(
                SupplementSpecification.withBookingId(bookingId));

        if (CollectionUtils.isEmpty(newSupplements) && existingSupplements.isEmpty()) {
            return true;
        }
        if (CollectionUtils.isEmpty(newSupplements) || existingSupplements.isEmpty()) {
            return false;
        }

        if (existingSupplements.size() != newSupplements.size()) {
            return false;
        }

        List<SupplementModel> sortedExisting = existingSupplements.stream()
                .sorted((a, b) -> a.getLabel().compareTo(b.getLabel()))
                .collect(Collectors.toList());

        List<SupplementPostResource> sortedNew = newSupplements.stream()
                .sorted((a, b) -> a.getLabel().compareTo(b.getLabel()))
                .collect(Collectors.toList());

        for (int i = 0; i < sortedExisting.size(); i++) {
            SupplementModel existing = sortedExisting.get(i);
            SupplementPostResource newSupplement = sortedNew.get(i);

            if (!existing.getLabel().equals(newSupplement.getLabel()) ||
                    !existing.getDescription().equals(newSupplement.getDescription()) ||
                    existing.getPrice().compareTo(newSupplement.getPrice()) != 0) {
                return false;
            }
        }

        return true;
    }

    private BookingModel createSingleBooking(BookingItemPostResource item, BookingModel groupBooking) {
        BookingModel singleBooking = bookingMapper.itemPostResourceToModel(item);
        singleBooking.setType(BookingTypeEnum.SINGLE);
        singleBooking.setStatus(BookingStatusEnum.DRAFT);
        singleBooking.setParentBooking(groupBooking);
        singleBooking.setParty(groupBooking.getParty());
        singleBooking.setSegmentId(groupBooking.getSegmentId());
        singleBooking.setSubSegmentId(groupBooking.getSubSegmentId());
        singleBooking.setOccupancy(item.getOccupancy());

        BigDecimal accommodationTotal = item.getNightlyRate()
                .multiply(BigDecimal.valueOf(item.getNights()))
                .multiply(BigDecimal.valueOf(item.getQuantity()));
        BigDecimal supplementsTotal = calculateSupplementsTotal(item, item.getQuantity());
        singleBooking.setTotal(accommodationTotal.add(supplementsTotal));

        singleBooking = bookingDaoService.save(singleBooking);
        saveSupplements(item.getSupplements(), singleBooking.getId());

        log.info("SINGLE booking created with ID: {}", singleBooking.getId());
        return singleBooking;
    }

    private BigDecimal calculateSupplementsTotal(BookingItemPostResource item, int quantity) {
        if (CollectionUtils.isEmpty(item.getSupplements())) {
            return BigDecimal.ZERO;
        }

        return item.getSupplements().stream()
                .map(supplement -> supplement.getPrice()
                        .multiply(BigDecimal.valueOf(item.getNights()))
                        .multiply(BigDecimal.valueOf(quantity)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void saveSupplements(List<SupplementPostResource> supplements, String bookingId) {
        if (CollectionUtils.isEmpty(supplements)) {
            return;
        }

        for (SupplementPostResource supplementPost : supplements) {
            SupplementModel supplement = bookingMapper.supplementPostResourceToModel(supplementPost);
            supplement.setBookingId(bookingId);
            supplementDaoService.save(supplement);
        }
    }

    private void deleteGroupBooking(BookingModel groupBooking) {
        List<BookingModel> singleBookings = findSingleBookings(groupBooking);

        for (BookingModel singleBooking : singleBookings) {
            supplementDaoService.deleteBy(SupplementSpecification.withBookingId(singleBooking.getId()));
            bookingDaoService.delete(singleBooking);
        }

        bookingDaoService.delete(groupBooking);
        log.info("Deleted GROUP booking and {} items", singleBookings.size());
    }

    private void deleteSingleBooking(BookingModel singleBooking) {
        BookingModel groupBooking = singleBooking.getParentBooking();

        supplementDaoService.deleteBy(SupplementSpecification.withBookingId(singleBooking.getId()));
        bookingDaoService.delete(singleBooking);

        // Check if GROUP has any remaining items
        List<BookingModel> remainingItems = findSingleBookings(groupBooking);
        if (remainingItems.isEmpty()) {
            bookingDaoService.delete(groupBooking);
            log.info("Deleted last item and GROUP booking");
        } else {
            log.info("Deleted SINGLE booking, {} items remaining", remainingItems.size());
        }
    }

    private ResponseEntity<BookingGetResource> handleStatusConfirmation(BookingModel booking, BookingPatchResource patchResource) {
        if (patchResource.getStatus() == BookingStatusEnum.CONFIRMED) {
            log.debug("Validating availability before confirmation for booking: {}", booking.getId());
            validateBookingAvailability(booking);

            booking.setStatus(BookingStatusEnum.CONFIRMED);
            bookingDaoService.save(booking);

            List<BookingModel> singleBookings = findSingleBookings(booking);
            for (BookingModel singleBooking : singleBookings) {
                singleBooking.setStatus(BookingStatusEnum.CONFIRMED);
                bookingDaoService.save(singleBooking);
            }

            // Apply GROUP → SINGLE logic if only one item
            if (singleBookings.size() == 1) {
                BookingModel singleBooking = singleBookings.get(0);
                singleBooking.setParentBooking(null);
                singleBooking.setType(BookingTypeEnum.SINGLE);

                // Copy GROUP fields to SINGLE
                singleBooking.setGuestName(booking.getGuestName());
                singleBooking.setPaymentMethod(booking.getPaymentMethod());
                singleBooking.setGuaranteeAmount(booking.getGuaranteeAmount());
                singleBooking.setSpecialNotes(booking.getSpecialNotes());

                bookingDaoService.save(singleBooking);
                bookingDaoService.delete(booking);

                log.info("Converted GROUP to SINGLE booking after confirmation");
                BookingGetResource response = bookingMapper.modelToGetResource(singleBooking);
                response.setItems(List.of(bookingMapper.modelToItemGetResource(singleBooking)));
                return ResponseEntity.ok(response);
            }

            log.info("Confirmed GROUP booking with {} items", singleBookings.size());
        }

        return retrieveById(booking.getId());
    }

    private ResponseEntity<BookingGetResource> handleBookingInfoUpdate(BookingModel booking, BookingPatchResource patchResource) {
        boolean updated = false;

        if (patchResource.getGuestName() != null) {
            booking.setGuestName(patchResource.getGuestName());
            updated = true;
        }
        if (patchResource.getPaymentMethod() != null) {
            booking.setPaymentMethod(patchResource.getPaymentMethod());
            updated = true;
        }
        if (patchResource.getGuaranteeAmount() != null) {
            booking.setGuaranteeAmount(patchResource.getGuaranteeAmount());
            updated = true;
        }
        if (patchResource.getSpecialNotes() != null) {
            booking.setSpecialNotes(patchResource.getSpecialNotes());
            updated = true;
        }
        if (patchResource.getEmail() != null || patchResource.getMobile() != null) {
            if (booking.getParty() != null && booking.getParty().getContact() != null) {
                if (patchResource.getEmail() != null) {
                    booking.getParty().getContact().setEmail(patchResource.getEmail());
                }
                if (patchResource.getMobile() != null) {
                    booking.getParty().getContact().setMobile(patchResource.getMobile());
                }
                updated = true;
            }
        }
        if (patchResource.getNightlyRate() != null) {
            booking.setNightlyRate(patchResource.getNightlyRate());
            recalculateItemTotal(booking);
            updated = true;
        }
        if (patchResource.getSupplements() != null) {
            updateBookingSupplements(booking, patchResource.getSupplements());
            updated = true;
        }

        if (updated) {
            bookingDaoService.save(booking);
            log.info("Updated booking information");
        }

        return retrieveById(booking.getId());
    }

    private void updateBookingSupplements(BookingModel booking, List<SupplementPostResource> newSupplements) {
        if (booking.getType() == BookingTypeEnum.SINGLE) {
            supplementDaoService.deleteBy(SupplementSpecification.withBookingId(booking.getId()));

            for (SupplementPostResource supplementPost : newSupplements) {
                SupplementModel supplement = bookingMapper.supplementPostResourceToModel(supplementPost);
                supplement.setBookingId(booking.getId());
                supplementDaoService.save(supplement);
            }

            recalculateItemTotal(booking);
            log.debug("Updated supplements for booking: {}", booking.getId());
        }
    }


    private void recalculateItemTotal(BookingModel booking) {
        if (booking.getType() == BookingTypeEnum.SINGLE) {
            BigDecimal accommodationTotal = booking.getNightlyRate()
                    .multiply(BigDecimal.valueOf(booking.getNights()))
                    .multiply(BigDecimal.valueOf(booking.getQuantity()));

            List<SupplementModel> supplements = supplementDaoService.findAllBy(
                    SupplementSpecification.withBookingId(booking.getId()));

            BigDecimal supplementsTotal = supplements.stream()
                    .map(s -> s.getPrice().multiply(BigDecimal.valueOf(booking.getNights()))
                            .multiply(BigDecimal.valueOf(booking.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            booking.setTotal(accommodationTotal.add(supplementsTotal));
        }
    }

    /**
     * Validates availability of units in a booking before confirmation.
     * Checks if any units are already booked by other CONFIRMED bookings in the same period.
     *
     * @param booking The booking to validate
     * @throws ConflictException if any units are not available
     */
    private void validateBookingAvailability(BookingModel booking) {
        log.debug("Validating availability for booking: {}", booking.getId());

        List<BookingModel> singleBookings = findSingleBookings(booking);

        for (BookingModel singleBooking : singleBookings) {
            if (isUnitNotAvailable(singleBooking)) {
                log.warn("Unit {} is not available for period {} to {}",
                        singleBooking.getUnit().getUnitId(),
                        singleBooking.getCheckinDate(),
                        singleBooking.getCheckoutDate());

                throw new ConflictException(
                        ConflictExceptionTitleEnum.UNIT_NOT_AVAILABLE,
                        String.format("Unit %s is not available for the selected period",
                                singleBooking.getUnit().getUnitName())
                );
            }
        }

        log.debug("All units are available for booking: {}", booking.getId());
    }

    /**
     * Checks if a unit is NOT available for the given booking item.
     * Excludes the current booking from availability check.
     *
     * @param singleBooking The booking item to check
     * @return true if unit is NOT available (conflict exists)
     */
    private boolean isUnitNotAvailable(BookingModel singleBooking) {
        String parentBookingId = singleBooking.getParentBooking() != null ?
                singleBooking.getParentBooking().getId() : singleBooking.getId();

        List<String> bookedUnits = bookingDaoService.findBookedUnitIdsExcludingBooking(
                singleBooking.getCheckinDate(),
                singleBooking.getCheckoutDate(),
                true, // strict mode
                parentBookingId
        );

        return bookedUnits.contains(singleBooking.getUnit().getUnitId());
    }
}