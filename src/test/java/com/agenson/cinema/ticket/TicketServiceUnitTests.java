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
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;
import java.util.stream.Collectors;

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

    private UserDB defaultUser;

    private RoomDB defaultRoom;

    private OrderDB defaultOrder;

    @BeforeEach
    public void setup() {
        this.defaultUser = new UserDB("username", new BCryptPasswordEncoder().encode("password"));
        this.defaultRoom = new RoomDB(99, 10, 20);
        this.defaultOrder = new OrderDB(this.defaultUser);
    }

    @Test
    public void findTicket_ShouldReturnTicket_WhenGivenUuid() {
        TicketDB ticket = new TicketDB(this.defaultRoom, null, Seat.fromString("A01"));

        when(this.ticketRepository.findByUuid(ticket.getUuid())).thenReturn(Optional.of(ticket));

        TicketDetailsDTO expected = new TicketDetailsDTO(ticket);
        Optional<TicketDetailsDTO> actual = this.ticketService.findTicket(ticket.getUuid());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void findTicket_ShouldReturnNull_WhenGivenUnknownUuid() {
        when(this.ticketRepository.findByUuid(any(UUID.class))).thenReturn(Optional.empty());

        assertThat(this.ticketService.findTicket(UUID.randomUUID()).isPresent()).isFalse();
        assertThat(this.ticketService.findTicket(null).isPresent()).isFalse();
    }

    @Test
    public void findTickets_ShouldReturnTicketList() {
        List<TicketDB> ticketList = Arrays.asList(
                new TicketDB(this.defaultRoom, null, Seat.fromString("A01")),
                new TicketDB(this.defaultRoom, null, Seat.fromString("A02")));

        when(this.ticketRepository.findAll()).thenReturn(ticketList);

        List<TicketDetailsDTO> actual = this.ticketService.findTickets();
        List<TicketDetailsDTO> expected = ticketList.stream().map(TicketDetailsDTO::new).collect(Collectors.toList());

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsOnlyOnceElementsOf(expected);
    }

    @Test
    public void findOrderTickets_ShouldReturnFilteredTicketList_WhenGivenOrderUuid() {
        OrderDB order1 = new OrderDB(this.defaultUser);
        OrderDB order2 = new OrderDB(this.defaultUser);

        TicketDB ticket1 = new TicketDB(this.defaultRoom, order1, Seat.fromString("A01"));
        TicketDB ticket2 = new TicketDB(this.defaultRoom, order2, Seat.fromString("A02"));
        List<TicketDB> ticketList = Arrays.asList(ticket1, ticket2);

        order1.setTickets(ticketList.stream()
                .filter(ticket -> ticket.getOrder() == order1)
                .collect(Collectors.toList()));

        when(this.orderRepository.findByUuid(order1.getUuid())).thenReturn(Optional.of(order1));

        List<TicketDetailsDTO> actual = this.ticketService.findOrderTickets(order1.getUuid());
        List<TicketDetailsDTO> expected = Collections.singletonList(new TicketDetailsDTO(ticket1));

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsOnlyOnceElementsOf(expected);
    }

    @Test
    public void findRoomTickets_ShouldReturnFilteredTicketList_WhenGivenRoomUuid() {
        RoomDB room1 = new RoomDB(1, 10, 20);
        RoomDB room2 = new RoomDB(2, 20, 30);

        TicketDB ticket1 = new TicketDB(room1, null, Seat.fromString("A01"));
        TicketDB ticket2 = new TicketDB(room2, null, Seat.fromString("A02"));
        List<TicketDB> ticketList = Arrays.asList(ticket1, ticket2);

        room1.setTickets(ticketList.stream()
                .filter(ticket -> ticket.getRoom() == room1)
                .collect(Collectors.toList()));

        when(this.roomRepository.findByUuid(room1.getUuid())).thenReturn(Optional.of(room1));

        List<TicketDetailsDTO> actual = this.ticketService.findRoomTickets(room1.getUuid());
        List<TicketDetailsDTO> expected = Collections.singletonList(new TicketDetailsDTO(ticket1));

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsOnlyOnceElementsOf(expected);
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
