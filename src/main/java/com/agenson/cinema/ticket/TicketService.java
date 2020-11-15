package com.agenson.cinema.ticket;

import com.agenson.cinema.order.OrderDB;
import com.agenson.cinema.order.OrderRepository;
import com.agenson.cinema.room.RoomDB;
import com.agenson.cinema.room.RoomRepository;
import com.agenson.cinema.security.restriction.RestrictToStaff;
import com.agenson.cinema.ticket.seat.Seat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;

    private final RoomRepository roomRepository;

    private final OrderRepository orderRepository;

    public Optional<TicketDetailsDTO> findTicket(UUID uuid) {
        return this.ticketRepository.findByUuid(uuid).map(TicketDetailsDTO::new);
    }

    @RestrictToStaff
    public List<TicketDetailsDTO> findTickets() {
        return this.ticketRepository.findAll().stream().map(TicketDetailsDTO::new).collect(Collectors.toList());
    }

    public List<TicketDetailsDTO> findOrderTickets(UUID uuid) {
        return this.orderRepository.findByUuid(uuid).map(OrderDB::getTickets)
                .map(tickets -> tickets.stream().map(TicketDetailsDTO::new).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @RestrictToStaff
    public List<TicketDetailsDTO> findRoomTickets(UUID uuid) {
        return this.roomRepository.findByUuid(uuid).map(RoomDB::getTickets)
                .map(tickets -> tickets.stream().map(TicketDetailsDTO::new).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

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

    @RestrictToStaff
    public void removeTicket(UUID uuid) {
        this.ticketRepository.deleteByUuid(uuid);
    }
}
