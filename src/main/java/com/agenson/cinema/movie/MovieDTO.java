package com.agenson.cinema.movie;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieDTO {

    private UUID uuid;
    private String title;
}
