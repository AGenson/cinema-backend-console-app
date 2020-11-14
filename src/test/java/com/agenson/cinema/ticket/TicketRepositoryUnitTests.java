package com.agenson.cinema.ticket;

import com.agenson.cinema.room.RoomDB;
import com.agenson.cinema.ticket.seat.Seat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class TicketRepositoryUnitTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TicketRepository ticketRepository;

    private TicketDB expected;

    @BeforeEach
    public void setup() {
        RoomDB room = new RoomDB(1, 10, 20);
        TicketDB ticket = new TicketDB(room, null, Seat.fromString("A01"));

        this.entityManager.persist(room);
        this.expected = this.entityManager.persist(ticket);
    }

    @Test
    public void findByUuid_ShouldReturnTicket_WhenGivenPersistedUuid() {
        Optional<TicketDB> actual = this.ticketRepository.findByUuid(this.expected.getUuid());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(this.expected);
    }

    @Test
    public void findByUuid_ShouldReturnNull_WhenGivenUnknownUuid() {
        Optional<TicketDB> actual = this.ticketRepository.findByUuid(UUID.randomUUID());

        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    public void deleteByUuid_ShouldDeleteTicket_WhenGivenUuid() {
        this.ticketRepository.deleteByUuid(this.expected.getUuid());

        Optional<TicketDB> actual = this.ticketRepository.findById(this.expected.getId());

        assertThat(actual.isPresent()).isFalse();
    }
}
