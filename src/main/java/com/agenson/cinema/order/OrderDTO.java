package com.agenson.cinema.order;

import com.agenson.cinema.ticket.TicketDetailsDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@EqualsAndHashCode
public class OrderDTO {

    private final UUID uuid;
    private final List<TicketDetailsDTO> tickets;

    public OrderDTO(OrderDB order) {
        this.uuid = order.getUuid();
        this.tickets = order.getTickets().stream()
                .map(TicketDetailsDTO::new)
                .collect(Collectors.toList());
    }
}
