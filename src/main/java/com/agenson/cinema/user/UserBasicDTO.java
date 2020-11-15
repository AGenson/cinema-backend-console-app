package com.agenson.cinema.user;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@Getter
@EqualsAndHashCode
public class UserBasicDTO {

    private final UUID uuid;
    private final String username;

    public UserBasicDTO(UserDB user) {
        this.uuid = user.getUuid();
        this.username = user.getUsername();
    }
}
