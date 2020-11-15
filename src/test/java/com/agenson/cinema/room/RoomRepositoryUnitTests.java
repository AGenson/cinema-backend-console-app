package com.agenson.cinema.room;

import com.agenson.cinema.ticket.TicketDB;
import com.agenson.cinema.ticket.seat.Seat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RoomRepositoryUnitTests implements RoomConstants {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoomRepository roomRepository;

    private RoomDB expected;

    @BeforeEach
    public void setup() {
        this.expected = this.entityManager.persist(new RoomDB(NORMAL_NUMBER, NORMAL_ROWS, NORMAL_COLS));
    }

    @Test
    public void findByUuid_ShouldReturnRoom_WhenGivenPersistedUuid() {
        Optional<RoomDB> actual = this.roomRepository.findByUuid(this.expected.getUuid());

        assertThat(actual).isNotEmpty();
        assertThat(actual.get()).isEqualTo(this.expected);
    }

    @Test
    public void findByUuid_ShouldReturnNull_WhenGivenUnknownUuid() {
        Optional<RoomDB> actual = this.roomRepository.findByUuid(UUID.randomUUID());

        assertThat(actual).isEmpty();
    }

    @Test
    public void findByNumber_ShouldReturnRoom_WhenGivenPersistedRoomNumber() {
        Optional<RoomDB> actual = this.roomRepository.findByNumber(this.expected.getNumber());

        assertThat(actual).isNotEmpty();
        assertThat(actual.get()).isEqualTo(this.expected);
    }

    @Test
    public void findByNumber_ShouldReturnNull_WhenGivenUnknownRoomNumber() {
        Optional<RoomDB> actual = this.roomRepository.findByNumber(UNKNOWN_NUMBER);

        assertThat(actual).isEmpty();
    }

    @Test
    public void deleteByUuid_ShouldDeleteRoom_WhenGivenUuid() {
        this.roomRepository.deleteByUuid(this.expected.getUuid());

        Optional<RoomDB> actual = this.roomRepository.findById(this.expected.getId());

        assertThat(actual).isEmpty();
    }

    @Test
    public void deleteByUuid_ShouldDeleteTickets_WhenGivenUuid() {
        TicketDB ticket = new TicketDB(this.expected, null, Seat.fromString("A01"));

        ticket = this.entityManager.persist(ticket);

        this.entityManager.refresh(this.expected);
        this.entityManager.refresh(ticket);

        assertThat(this.entityManager.find(TicketDB.class, ticket.getId())).isNotNull();
        assertThat(this.expected.getTickets()).containsOnly(ticket);
        assertThat(ticket.getRoom()).isEqualTo(this.expected);

        this.roomRepository.deleteByUuid(this.expected.getUuid());
        this.entityManager.flush();

        assertThat(this.entityManager.find(TicketDB.class, ticket.getId())).isNull();
    }
}
