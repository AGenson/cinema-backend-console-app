package com.agenson.cinema.movie;

public class InvalidMovieException extends RuntimeException {

    public enum Type {
        MANDATORY("Error: Title is mandatory"),
        MAXSIZE("Error: Title must be less than 32 characters"),
        EXISTS("Error: Title already exists");

        private final String type;

        Type(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return this.type;
        }
    }

    public InvalidMovieException(Type type) {
        super(type.toString());
    }
}
