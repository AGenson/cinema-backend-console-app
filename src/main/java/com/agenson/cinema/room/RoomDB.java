package com.agenson.cinema.room;

import com.agenson.cinema.movie.MovieDB;
import com.agenson.cinema.ticket.TicketDB;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.Collections;
import java.util.List;
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

    // SQL Foreign Key Constraint Definition: ON DELETE SET NULL
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "movie_id")
    private MovieDB movie = null;

    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "room", cascade = CascadeType.REMOVE)
    private List<TicketDB> tickets = Collections.emptyList();

    public RoomDB(int number, int nbRows, int nbCols) {
        this.number = number;
        this.nbRows = nbRows;
        this.nbCols = nbCols;
    }
}
