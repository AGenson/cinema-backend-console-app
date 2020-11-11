package com.agenson.cinema.movie;

import com.agenson.cinema.room.RoomDB;
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
@Table(name = "movie")
public class MovieDB {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id = -1L;
    protected UUID uuid = UUID.randomUUID();
    private String title = "";

    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "movie")
    private List<RoomDB> rooms = Collections.emptyList();

    public MovieDB(String title) {
        this.title = title;
    }
}
