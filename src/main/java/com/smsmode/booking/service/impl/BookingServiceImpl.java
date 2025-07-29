package com.smsmode.booking.service.impl;

import com.smsmode.booking.dao.service.BookingDaoService;
import com.smsmode.booking.dao.service.SupplementDaoService;
import com.smsmode.booking.dao.specification.BookingSpecification;
import com.smsmode.booking.dao.specification.SupplementSpecification;
import com.smsmode.booking.embeddable.ChildEmbeddable;
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

        BookingModel groupBooking = findGroupBooking(bookingId);
        BookingModel existingItem = findExistingItem(groupBooking, bookingItemPostResource);

        if (existingItem != null) {
            handleExistingItem(existingItem, bookingItemPostResource);
        } else {
            createSingleBooking(bookingItemPostResource, groupBooking);
            log.info("Created new item");
        }

        return retrieveById(bookingId);
    }

    @Override
    public ResponseEntity<BookingGetResource> retrieveById(String bookingId) {
        log.debug("Retrieving booking: {}", bookingId);

        BookingModel groupBooking = findGroupBooking(bookingId);
        List<BookingModel> singleBookings = findSingleBookings(groupBooking);
        BookingGetResource response = buildBookingResponse(groupBooking, singleBookings);

        return ResponseEntity.ok(response);
    }

    private BookingModel findGroupBooking(String bookingId) {
        return bookingDaoService.findOneBy(
                BookingSpecification.withIdEqual(bookingId).and(BookingSpecification.withType(BookingTypeEnum.GROUP)));
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
            List<SupplementPostResource> supplementResources = supplements.stream()
                    .map(this::mapSupplementToResource)
                    .collect(Collectors.toList());
            itemResource.setSupplements(supplementResources);

            itemResources.add(itemResource);
        }

        response.setItems(itemResources);
        return response;
    }

    private SupplementPostResource mapSupplementToResource(SupplementModel supplement) {
        SupplementPostResource resource = new SupplementPostResource();
        resource.setLabel(supplement.getLabel());
        resource.setDescription(supplement.getDescription());
        resource.setPrice(supplement.getPrice());
        return resource;
    }

    private void handleExistingItem(BookingModel existingItem, BookingItemPostResource itemRequest) {
        int finalQuantity = itemRequest.getQuantity();

        if (finalQuantity == 0) {
            deleteItem(existingItem);
        } else {
            updateItemQuantity(existingItem, itemRequest, finalQuantity);
        }
    }

    private void deleteItem(BookingModel existingItem) {
        supplementDaoService.deleteBy(SupplementSpecification.withBookingId(existingItem.getId()));
        bookingDaoService.delete(existingItem);
        log.info("Deleted item completely");
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
        return existing.getAdults().equals(newItem.getAdults()) &&
                isSameChildren(existing.getChildren(), newItem.getChildren());
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

        BigDecimal accommodationTotal = item.getNightlyRate().multiply(BigDecimal.valueOf(item.getNights()));
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
            SupplementModel supplement = new SupplementModel();
            supplement.setLabel(supplementPost.getLabel());
            supplement.setDescription(supplementPost.getDescription());
            supplement.setPrice(supplementPost.getPrice());
            supplement.setBookingId(bookingId);
            supplementDaoService.save(supplement);
        }
    }
}



