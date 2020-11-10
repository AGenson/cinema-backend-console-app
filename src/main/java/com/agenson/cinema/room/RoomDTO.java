package com.agenson.cinema.room;

import com.agenson.cinema.movie.MovieDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {

    private UUID uuid;
    private int number;
    private int nbRows;
    private int nbCols;
    private MovieDTO movie;
}
