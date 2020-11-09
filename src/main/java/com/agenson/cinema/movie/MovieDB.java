package com.agenson.cinema.movie;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "movie")
public class MovieDB {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id = -1L;
    protected UUID uuid = UUID.randomUUID();
    private String title = "";

    public MovieDB(String title) {
        this.title = title;
    }
}
