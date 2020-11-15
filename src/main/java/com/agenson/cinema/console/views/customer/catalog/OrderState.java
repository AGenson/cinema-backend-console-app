package com.agenson.cinema.console.views.customer.catalog;

import com.agenson.cinema.room.RoomDTO;
import com.agenson.cinema.ticket.seat.Seat;
import com.agenson.cinema.user.UserBasicDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class OrderState {

    private final UserBasicDTO user;
    private final RoomDTO room;
    private final List<Seat> seats;
}
