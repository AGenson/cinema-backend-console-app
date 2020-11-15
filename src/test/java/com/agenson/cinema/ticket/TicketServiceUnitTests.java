package com.agenson.cinema.ticket;

import com.agenson.cinema.order.OrderDB;
import com.agenson.cinema.order.OrderRepository;
import com.agenson.cinema.room.RoomDB;
import com.agenson.cinema.room.RoomRepository;
import com.agenson.cinema.ticket.seat.Seat;
import com.agenson.cinema.user.UserDB;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TicketServiceUnitTests {

    private static final HashMap<Seat, InvalidTicketException.Type> INVALID_TICKET_SEATS =
            new HashMap<Seat, InvalidTicketException.Type>() {{
                put(null, InvalidTicketException.Type.SEAT);
                put(Seat.fromString("Z01"), InvalidTicketException.Type.CAPACITY);
                put(Seat.fromString("A50"), InvalidTicketException.Type.CAPACITY);
                put(Seat.fromString("A01"), InvalidTicketException.Type.EXISTS);
            }};

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private TicketService ticketService;

    private RoomDB defaultRoom;

    private OrderDB defaultOrder;

    @BeforeEach
    public void setup() {
        UserDB defaultUser = new UserDB("username", new BCryptPasswordEncoder().encode("password"));

        this.defaultRoom = new RoomDB(99, 10, 20);
        this.defaultOrder = new OrderDB(defaultUser);
    }

    @Test
    public void createTicket_ShouldReturnTicket_WhenGivenRoomAndOrderUuidAndSeat() {
        Seat seat = Seat.fromString("A01");

        when(this.ticketRepository.save(any(TicketDB.class))).then(returnsFirstArg());
        when(this.roomRepository.findByUuid(this.defaultRoom.getUuid())).thenReturn(Optional.of(this.defaultRoom));
        when(this.orderRepository.findByUuid(this.defaultOrder.getUuid())).thenReturn(Optional.of(this.defaultOrder));

        TicketDetailsDTO actual = this.ticketService.createTicket(
                this.defaultRoom.getUuid(),
                this.defaultOrder.getUuid(),
                seat
        );

        assertThat(actual.getUuid()).isNotNull();
        assertThat(actual.getSeat()).isEqualTo(seat);
        assertThat(actual.getRoom().getUuid()).isEqualTo(this.defaultRoom.getUuid());
    }

    @Test
    public void createTicket_ShouldThrowAssociatedInvalidTicketException_WhenGivenInvalidRoomUuid() {
        Seat seat = Seat.fromString("A01");

        when(this.roomRepository.findByUuid(any(UUID.class))).thenReturn(Optional.empty());
        when(this.roomRepository.findByUuid(null)).thenReturn(Optional.empty());
        lenient().when(this.orderRepository.findByUuid(this.defaultOrder.getUuid()))
                .thenReturn(Optional.of(this.defaultOrder));

        for (UUID uuid : Arrays.asList(null, UUID.randomUUID()))
            assertThatExceptionOfType(InvalidTicketException.class)
                    .isThrownBy(() -> this.ticketService.createTicket(uuid, this.defaultOrder.getUuid(), seat))
                    .withMessage(InvalidTicketException.Type.ROOM.toString());
    }

    @Test
    public void createTicket_ShouldThrowAssociatedInvalidTicketException_WhenGivenInvalidOrderUuid() {
        Seat seat = Seat.fromString("A01");

        when(this.orderRepository.findByUuid(any(UUID.class))).thenReturn(Optional.empty());
        when(this.orderRepository.findByUuid(null)).thenReturn(Optional.empty());
        lenient().when(this.roomRepository.findByUuid(this.defaultRoom.getUuid()))
                .thenReturn(Optional.of(this.defaultRoom));

        for (UUID uuid : Arrays.asList(null, UUID.randomUUID()))
            assertThatExceptionOfType(InvalidTicketException.class)
                    .isThrownBy(() -> this.ticketService.createTicket(this.defaultRoom.getUuid(), uuid, seat))
                    .withMessage(InvalidTicketException.Type.ORDER.toString());
    }

    @Test
    public void createTicket_ShouldThrowAssociatedInvalidTicketException_WhenGivenInvalidSeat() {
        Seat seat = Seat.fromString("A01");

        this.defaultRoom.setTickets(Collections.singletonList(new TicketDB(this.defaultRoom, null, seat)));
        when(this.orderRepository.findByUuid(this.defaultOrder.getUuid())).thenReturn(Optional.of(this.defaultOrder));
        when(this.roomRepository.findByUuid(this.defaultRoom.getUuid())).thenReturn(Optional.of(this.defaultRoom));

        for (Map.Entry<Seat, InvalidTicketException.Type> pair : INVALID_TICKET_SEATS.entrySet())
            assertThatExceptionOfType(InvalidTicketException.class)
                    .isThrownBy(() -> this.ticketService.createTicket(
                            this.defaultRoom.getUuid(),
                            this.defaultOrder.getUuid(),
                            pair.getKey())
                    ).withMessage(pair.getValue().toString());
    }
}
