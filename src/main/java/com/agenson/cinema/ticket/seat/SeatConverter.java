package com.agenson.cinema.ticket.seat;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class SeatConverter implements AttributeConverter<Seat, String> {

    @Override
    public String convertToDatabaseColumn(Seat seat) {
        return seat.toString();
    }

    @Override
    public Seat convertToEntityAttribute(String seat) {
        return Seat.fromString(seat);
    }
}
