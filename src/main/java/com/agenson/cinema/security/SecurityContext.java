package com.agenson.cinema.security;

import lombok.Value;

import java.util.UUID;

public class SecurityContext {

    @Value
    private static class UserDetails {

        UUID uuid;
        UserRole role;
    }

    private UserDetails currentUser = null;

    public void login(UUID uuid, UserRole role) {
        this.currentUser = new UserDetails(uuid, role);
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean isLoggedIn() {
        return this.currentUser != null;
    }

    public boolean hasRole(UserRole role) {
        return this.isLoggedIn() && this.currentUser.getRole() == role;
    }

    public boolean isUser(UUID uuid) {
        return this.isLoggedIn() && this.currentUser.getUuid() == uuid;
    }
}
