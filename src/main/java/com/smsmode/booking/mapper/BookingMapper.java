package com.smsmode.booking.mapper;

import com.smsmode.booking.model.BookingModel;
import com.smsmode.booking.model.base.AbstractBaseModel;

import com.smsmode.booking.resource.booking.BookingGetResource;
import com.smsmode.booking.resource.booking.BookingItemGetResource;
import com.smsmode.booking.resource.booking.BookingItemPostResource;
import com.smsmode.booking.resource.booking.BookingPostResource;
import com.smsmode.booking.resource.common.AuditGetResource;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.*;

import java.time.format.DateTimeFormatter;

/**
 * Mapper for BookingModel and resources
 *
 * @author hamzahabchi (contact: hamza.habchi@messaging-technologies.com)
 * <p>Created 07 Jul 2025</p>
 */
@Slf4j
@Mapper(
        componentModel = "spring",
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class BookingMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public abstract BookingGetResource modelToGetResource(BookingModel bookingModel);

    @AfterMapping
    public void afterModelToGetResource(BookingModel bookingModel, @MappingTarget BookingGetResource bookingGetResource) {
        bookingGetResource.setAudit(this.modelToAuditResource(bookingModel));
    }

    public abstract BookingItemGetResource modelToItemGetResource(BookingModel bookingModel);

    public abstract BookingModel postResourceToModel(BookingPostResource bookingPostResource);

    @Mapping(target = "checkinDate", source = "checkinDate", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "checkoutDate", source = "checkoutDate", qualifiedByName = "stringToLocalDate")
    public abstract BookingModel itemPostResourceToModel(BookingItemPostResource bookingItemPostResource);

    public abstract AuditGetResource modelToAuditResource(AbstractBaseModel baseModel);

    @Named("stringToLocalDate")
    protected java.time.LocalDate stringToLocalDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        return java.time.LocalDate.parse(dateString, DATE_FORMATTER);
    }
}