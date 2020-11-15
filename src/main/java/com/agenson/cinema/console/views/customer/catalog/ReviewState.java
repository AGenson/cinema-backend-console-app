package com.agenson.cinema.console.views.customer.catalog;

import com.agenson.cinema.room.RoomDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ReviewState {

    private final RoomDTO room;
    private final List<String> ticketsStatus;
}
