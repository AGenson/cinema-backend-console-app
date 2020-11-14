package com.agenson.cinema.user;

public class InvalidUserException extends RuntimeException {

    public enum Type {
        USERNAME_MANDATORY("Error: Username is mandatory"),
        USERNAME_MAXSIZE("Error: Username must be less than 16 characters"),
        USERNAME_EXISTS("Error: Username is already used"),
        PASSWORD_MANDATORY("Error: Password is mandatory"),
        PASSWORD_MAXSIZE("Error: Password must be less than 16 characters"),
        PASSWORD_CONFIRM("Error: Passwords must match");

        private final String type;

        Type(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return this.type;
        }
    }

    public InvalidUserException(Type type) {
        super(type.toString());
    }
}
