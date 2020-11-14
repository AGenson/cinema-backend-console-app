package com.agenson.cinema.ticket;

public class InvalidTicketException extends RuntimeException {

    public enum Type {
        ROOM("Error: Room not found"),
        ORDER("Error: Order not found"),
        SEAT("Error: Seat is mandatory"),
        CAPACITY("Error: Seat is out of boundary"),
        EXISTS("Error: Seat already reserved");

        private final String type;

        Type(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return this.type;
        }
    }

    public InvalidTicketException(InvalidTicketException.Type type) {
        super(type.toString());
    }
}
