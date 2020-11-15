package com.agenson.cinema.ticket;

import com.agenson.cinema.order.OrderDB;
import com.agenson.cinema.order.OrderRepository;
import com.agenson.cinema.room.RoomDB;
import com.agenson.cinema.room.RoomRepository;
import com.agenson.cinema.ticket.seat.Seat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;

    private final RoomRepository roomRepository;

    private final OrderRepository orderRepository;

    public TicketDetailsDTO createTicket(UUID roomUuid, UUID orderUuid, Seat seat) {
        RoomDB room = this.roomRepository.findByUuid(roomUuid)
                .orElseThrow(() -> new InvalidTicketException(InvalidTicketException.Type.ROOM));

        OrderDB order = this.orderRepository.findByUuid(orderUuid)
                .orElseThrow(() -> new InvalidTicketException(InvalidTicketException.Type.ORDER));

        if (seat == null)
            throw new InvalidTicketException(InvalidTicketException.Type.SEAT);
        else if (seat.getCol() > room.getNbCols() || seat.getRow() > room.getNbRows())
            throw new InvalidTicketException(InvalidTicketException.Type.CAPACITY);
        else if(room.getTickets().stream().anyMatch(ticket -> ticket.getSeat().equals(seat)))
            throw new InvalidTicketException(InvalidTicketException.Type.EXISTS);

        TicketDB ticket = this.ticketRepository.save(new TicketDB(room, order, seat));

        return new TicketDetailsDTO(ticket);
    }
}
