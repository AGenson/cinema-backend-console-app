package com.agenson.cinema.ticket;

import com.agenson.cinema.order.OrderDB;
import com.agenson.cinema.room.RoomDB;
import com.agenson.cinema.security.SecurityService;
import com.agenson.cinema.security.SecurityRole;
import com.agenson.cinema.ticket.seat.Seat;
import com.agenson.cinema.user.UserDB;
import com.agenson.cinema.utils.CallableOneArgument;
import com.agenson.cinema.utils.StaffSecurityAssertion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;

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
    private ModelMapper mapper;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketService ticketService;

    private UserDB defaultUser;

    private RoomDB defaultRoom;

    private OrderDB defaultOrder;

    @BeforeEach
    public void setup() {
        String encodedPassword = new BCryptPasswordEncoder().encode("password");
        UserDB user = new UserDB("username", encodedPassword);
        RoomDB room = new RoomDB(99, 10, 20);

        user.setRole(SecurityRole.STAFF);
        OrderDB order = new OrderDB(user);

        this.entityManager.persist(user);
        this.entityManager.persist(room);
        this.entityManager.persist(order);

        this.defaultUser = user;
        this.defaultRoom = room;
        this.defaultOrder = order;

        this.loginAs(this.defaultUser.getRole());
    }

    @AfterEach
    public void logout() {
        this.securityService.logout();
    }

    @Test
    public void findTicket_ShouldReturnPersistedTicket_WhenGivenUuid() {
        TicketDB ticket = new TicketDB(this.defaultRoom, null, Seat.fromString("A01"));

        this.ticketRepository.save(ticket);

        TicketDTO expected = this.mapper.map(ticket, TicketDTO.class);
        Optional<TicketDTO> actual = this.ticketService.findTicket(ticket.getUuid());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void findTicket_ShouldReturnNull_WhenNotFoundWithUuid() {
        assertThat(this.ticketService.findTicket(UUID.randomUUID()).isPresent()).isFalse();
    }

    @Test
    public void findTickets_ShouldReturnTicketList() {
        List<TicketDB> ticketList = Arrays.asList(
                new TicketDB(this.defaultRoom, this.defaultOrder, Seat.fromString("A01")),
                new TicketDB(this.defaultRoom, this.defaultOrder, Seat.fromString("A02")));

        assertThat(this.ticketRepository.findAll().size()).isZero();

        this.ticketRepository.saveAll(ticketList);

        List<TicketDTO> actual = this.ticketService.findTickets();
        List<TicketDTO> expected = ticketList.stream()
                .map(ticket -> this.mapper.map(ticket, TicketDTO.class))
                .collect(Collectors.toList());

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsOnlyOnceElementsOf(expected);
    }

    @Test
    public void findTickets_ShouldThrowSecurityException_WhenNotLoggedInAsStaff() {
        StaffSecurityAssertion.assertShouldThrowSecurityException(
                () -> this.ticketService.findTickets(),
                () -> this.loginAs(SecurityRole.CUSTOMER),
                () -> this.logout()
        );
    }

    @Test
    public void findOrderTickets_ShouldReturnFilteredTicketList_WhenGivenOrderUuid() {
        OrderDB order1 = new OrderDB(this.defaultUser);
        OrderDB order2 = new OrderDB(this.defaultUser);

        TicketDB ticket1 = new TicketDB(this.defaultRoom, order1, Seat.fromString("A01"));
        TicketDB ticket2 = new TicketDB(this.defaultRoom, order2, Seat.fromString("A02"));
        List<TicketDB> ticketList = Arrays.asList(ticket1, ticket2);

        assertThat(this.ticketRepository.findAll().size()).isZero();

        this.entityManager.persist(order1);
        this.entityManager.persist(order2);
        this.ticketRepository.saveAll(ticketList);
        this.entityManager.refresh(order1);

        List<TicketDTO> actual = this.ticketService.findOrderTickets(order1.getUuid());
        List<TicketDTO> expected = Collections.singletonList(this.mapper.map(ticket1, TicketDTO.class));

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsOnlyOnceElementsOf(expected);
    }

    @Test
    public void findRoomTickets_ShouldReturnFilteredTicketList_WhenGivenRoomUuid() {
        RoomDB room1 = new RoomDB(1, 10, 20);
        RoomDB room2 = new RoomDB(2, 20, 30);

        TicketDB ticket1 = new TicketDB(room1, this.defaultOrder, Seat.fromString("A01"));
        TicketDB ticket2 = new TicketDB(room2, this.defaultOrder, Seat.fromString("A02"));
        List<TicketDB> ticketList = Arrays.asList(ticket1, ticket2);

        this.entityManager.persist(room1);
        this.entityManager.persist(room2);
        this.ticketRepository.saveAll(ticketList);
        this.entityManager.refresh(room1);

        List<TicketDTO> actual = this.ticketService.findRoomTickets(room1.getUuid());
        List<TicketDTO> expected = Collections.singletonList(this.mapper.map(ticket1, TicketDTO.class));

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsOnlyOnceElementsOf(expected);
    }

    @Test
    public void findRoomTickets_ShouldThrowSecurityException_WhenNotLoggedInAsStaff() {
        StaffSecurityAssertion.assertShouldThrowSecurityException(
                () -> this.ticketService.findRoomTickets(UUID.randomUUID()),
                () -> this.loginAs(SecurityRole.CUSTOMER),
                () -> this.logout()
        );
    }

    @Test
    public void createTicket_ShouldReturnPersistedTicket_WhenGivenRoomAndOrderUuidAndSeat() {
        TicketDTO expected = this.ticketService.createTicket(
                this.defaultRoom.getUuid(),
                this.defaultOrder.getUuid(),
                Seat.fromString("A01"));

        Optional<TicketDTO> actual = this.ticketRepository.findByUuid(expected.getUuid())
                .map(ticket -> this.mapper.map(ticket, TicketDTO.class));

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
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

    @Test
    public void removeTicket_ShouldRemoveTicket_WhenGivenUuid() {
        TicketDB ticket = new TicketDB(this.defaultRoom, this.defaultOrder, Seat.fromString("A1"));

        this.ticketRepository.save(ticket);
        this.ticketService.removeTicket(ticket.getUuid());
        Optional<TicketDB> actual = this.ticketRepository.findByUuid(ticket.getUuid());

        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    public void removeTicket_ShouldThrowSecurityException_WhenNotLoggedInAsStaff() {
        StaffSecurityAssertion.assertShouldThrowSecurityException(
                () -> this.ticketService.removeTicket(UUID.randomUUID()),
                () -> this.loginAs(SecurityRole.CUSTOMER),
                () -> this.logout()
        );
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

    private void loginAs(SecurityRole role) {
        this.defaultUser.setRole(role);
        this.entityManager.persist(this.defaultUser);
        this.securityService.login("username", "password");
    }
}
