package com.smsmode.booking.embeddable;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Embeddable class representing occupancy information with adults and children.
 */
@Data
@Embeddable
public class OccupancyEmbeddable {
    private Integer adults;

    @ElementCollection
    @CollectionTable(name = "X_BOOKING_CHILDREN", joinColumns = @JoinColumn(name = "BOOKING_ID"))
    private List<ChildEmbeddable> children = new ArrayList<>();
}