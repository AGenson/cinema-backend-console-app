package com.agenson.cinema.user;

import com.agenson.cinema.security.SecurityRole;
import lombok.Value;

import java.util.UUID;

@Value
public class UserDetailsDTO {

    UUID uuid;
    String username;
    SecurityRole role;

    @Override
    public String toString() {
        if (SecurityRole.STAFF.equals(this.role))
            return this.username + " (STAFF)";
        else
            return this.username;
    }
}
