package com.smsmode.booking.embeddable;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class ChildEmbeddable {
    private Integer age;
    private Integer quantity;
}