package com.agenson.cinema.ticket.seat;

public class InvalidSeatException extends RuntimeException {

    public enum Type {
        ROW("Error: Seat row must be between A & Z"),
        COL("Error: Seat column is out of boundary"),
        FORMAT("Error: Invalid format for seat");

        private final String type;

        Type(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return this.type;
        }
    }

    public InvalidSeatException(InvalidSeatException.Type type) {
        super(type.toString());
    }
}
