package com.agenson.cinema.movie;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@Getter
@EqualsAndHashCode
public class MovieDTO {

    private final UUID uuid;
    private final String title;

    public MovieDTO(MovieDB movie) {
        this.uuid = movie.getUuid();
        this.title = movie.getTitle();
    }

    @Override
    public String toString() {
        return this.title;
    }
}
