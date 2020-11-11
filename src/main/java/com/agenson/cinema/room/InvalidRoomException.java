package com.agenson.cinema.room;


public class InvalidRoomException extends RuntimeException {

    public enum Type {
        NB_ROWS("Error: Number of rows needs to be > 0"),
        NB_COLS("Error: Number of columns needs to be > 0"),
        NUMBER("Error: Room number needs to be > 0"),
        EXISTS("Error: Room number is already used"),
        MOVIE("Error: Movie not found");

        private final String type;

        Type(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return this.type;
        }
    }

    public InvalidRoomException(InvalidRoomException.Type type) {
        super(type.toString());
    }
}
