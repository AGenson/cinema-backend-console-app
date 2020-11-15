package com.agenson.cinema.user;

import com.agenson.cinema.security.SecurityRole;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class UserDetailsDTO extends UserBasicDTO {

    private final SecurityRole role;

    public UserDetailsDTO(UserDB user) {
        super(user);
        this.role = user.getRole();
    }

    @Override
    public String toString() {
        if (SecurityRole.STAFF.equals(this.role))
            return this.getUsername() + " (STAFF)";
        else
            return this.getUsername();
    }
}
