package com.agenson.cinema.ticket;

import com.agenson.cinema.order.OrderDB;
import com.agenson.cinema.room.RoomDB;
import com.agenson.cinema.room.RoomDTO;
import com.agenson.cinema.security.SecurityService;
import com.agenson.cinema.security.SecurityRole;
import com.agenson.cinema.ticket.seat.Seat;
import com.agenson.cinema.user.UserDB;
import com.agenson.cinema.utils.CallableOneArgument;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TicketIntegrationTests {

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketService ticketService;

    private RoomDB defaultRoom;

    private OrderDB defaultOrder;

    @BeforeEach
    public void setup() {
        UserDB user = new UserDB("username", this.encoder.encode("password"));
        RoomDB room = new RoomDB(99, 10, 20);
        OrderDB order = new OrderDB(user);

        this.entityManager.persist(user);
        this.entityManager.persist(room);
        this.entityManager.persist(order);

        this.defaultRoom = room;
        this.defaultOrder = order;
    }

    @AfterEach
    public void logout() {
        this.securityService.logout();
    }

    @Test
    public void createTicket_ShouldReturnPersistedTicket_WhenGivenRoomAndOrderUuidAndSeat() {
        Seat seat = Seat.fromString("A01");
        TicketDetailsDTO actual = this.ticketService.createTicket(
                this.defaultRoom.getUuid(),
                this.defaultOrder.getUuid(),
                seat);

        assertThat(actual.getRoom()).isEqualTo(new RoomDTO(this.defaultRoom));
        assertThat(actual.getSeat()).isEqualTo(seat);
    }

    @Test
    public void createTicket_ShouldNotPersistTicket_WhenGivenInvalidRoomUuid() {
        this.assertShouldThrowInvalidTicketException_WhenGivenInvalidUuid(uuid -> {
            this.ticketService.createTicket(uuid, this.defaultOrder.getUuid(), Seat.fromString("A01"));
        });
    }

    @Test
    public void createTicket_ShouldThrowAssociatedInvalidTicketException_WhenGivenInvalidOrderUuid() {
        this.assertShouldThrowInvalidTicketException_WhenGivenInvalidUuid(uuid -> {
            this.ticketService.createTicket(this.defaultRoom.getUuid(), uuid, Seat.fromString("A01"));
        });
    }

    @Test
    public void createTicket_ShouldThrowAssociatedInvalidTicketException_WhenGivenInvalidSeat() {
        Seat seatAlreadyUsed = Seat.fromString("A01");

        this.ticketRepository.save(new TicketDB(this.defaultRoom, this.defaultOrder, seatAlreadyUsed));
        this.entityManager.refresh(this.defaultRoom);

        List<TicketDB> expected = this.ticketRepository.findAll();

        for (Seat seat : Arrays.asList(null, Seat.fromString("Z01"), Seat.fromString("A50"), seatAlreadyUsed)) {
            assertThatExceptionOfType(InvalidTicketException.class)
                    .isThrownBy(() -> {
                        this.ticketService.createTicket(this.defaultRoom.getUuid(), this.defaultOrder.getUuid(), seat);
                    });

            List<TicketDB> actual = this.ticketRepository.findAll();

            assertThat(actual.size()).isEqualTo(expected.size());
            assertThat(actual).containsOnlyOnceElementsOf(expected);
        }
    }

    private void assertShouldThrowInvalidTicketException_WhenGivenInvalidUuid(CallableOneArgument<UUID> callable) {
        this.ticketRepository.save(new TicketDB(this.defaultRoom, this.defaultOrder, Seat.fromString("A02")));

        List<TicketDB> expected = this.ticketRepository.findAll();

        for (UUID uuid : Arrays.asList(null, UUID.randomUUID())) {
            assertThatExceptionOfType(InvalidTicketException.class)
                    .isThrownBy(() -> callable.call(uuid));

            List<TicketDB> actual = this.ticketRepository.findAll();

            assertThat(actual.size()).isEqualTo(expected.size());
            assertThat(actual).containsOnlyOnceElementsOf(expected);
        }
    }
}
