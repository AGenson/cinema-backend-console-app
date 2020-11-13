package com.agenson.cinema.order;

public class InvalidOrderException extends RuntimeException {

    public enum Type {
        USER("Error: User was not found");

        private final String type;

        Type(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return this.type;
        }
    }

    public InvalidOrderException(Type type) {
        super(type.toString());
    }
}
