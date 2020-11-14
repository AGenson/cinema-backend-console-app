package com.agenson.cinema.ticket;

import com.agenson.cinema.order.OrderDB;
import com.agenson.cinema.room.RoomDB;
import com.agenson.cinema.ticket.seat.Seat;
import com.agenson.cinema.ticket.seat.SeatConverter;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "ticket")
public class TicketDB {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id = -1L;
    private UUID uuid = UUID.randomUUID();

    @Convert(converter = SeatConverter.class)
    private Seat seat;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "room_id")
    private RoomDB room;

    // SQL Foreign Key Constraint Definition: ON DELETE SET NULL
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id")
    private OrderDB order = null;

    public TicketDB(RoomDB room, OrderDB order, Seat seat) {
        this.room = room;
        this.order = order;
        this.seat = seat;
    }
}
