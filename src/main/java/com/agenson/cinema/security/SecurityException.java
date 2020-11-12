package com.agenson.cinema.security;

public class SecurityException extends RuntimeException {

    public enum Type {
        IDENTIFICATION("Error: Requires identification"),
        AUTHORIZATION("Error: Requires authorization"),
        CONNECTION("Error: Username or password is incorrect");

        private final String type;

        Type(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return this.type;
        }
    }

    public SecurityException(SecurityException.Type type) {
        super(type.toString());
    }
}
