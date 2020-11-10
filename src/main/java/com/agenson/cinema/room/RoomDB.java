package com.agenson.cinema.room;

import com.agenson.cinema.movie.MovieDB;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "room")
public class RoomDB {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id = -1L;
    private UUID uuid = UUID.randomUUID();
    private int number = -1;
    private int nbRows = -1;
    private int nbCols = -1;

    @ManyToOne(fetch = FetchType.EAGER)
    private MovieDB movie = null;

    public RoomDB(int number, int nbRows, int nbCols) {
        this.number = number;
        this.nbRows = nbRows;
        this.nbCols = nbCols;
    }
}
