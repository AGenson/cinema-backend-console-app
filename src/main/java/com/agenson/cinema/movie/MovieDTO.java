package com.agenson.cinema.movie;

import lombok.Value;

import java.util.UUID;

@Value
public class MovieDTO {

    UUID uuid;
    String title;

    public static MovieDTO from(MovieDB movie) {
        return new MovieDTO(movie.getUuid(), movie.getTitle());
    }
}
