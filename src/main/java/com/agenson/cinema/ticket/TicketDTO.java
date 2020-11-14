package com.agenson.cinema.ticket;

import com.agenson.cinema.order.OrderDTO;
import com.agenson.cinema.room.RoomDTO;
import com.agenson.cinema.ticket.seat.Seat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketDTO {

    private UUID uuid;
    private Seat seat;
    private RoomDTO room;
    private OrderDTO order;
}
