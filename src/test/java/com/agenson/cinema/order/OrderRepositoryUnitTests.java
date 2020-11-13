package com.agenson.cinema.order;

import com.agenson.cinema.user.UserDB;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class OrderRepositoryUnitTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    private OrderDB expected;

    @BeforeEach
    public void setup() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        UserDB user = new UserDB("username", encoder.encode("password"));

        this.entityManager.persist(user);
        expected = this.entityManager.persist(new OrderDB(user));
    }

    @Test
    public void findByUuid_ShouldReturnOrder_WhenGivenPersistedUuid() {
        Optional<OrderDB> actual = this.orderRepository.findByUuid(this.expected.getUuid());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(this.expected);
    }

    @Test
    public void findByUuid_ShouldReturnNull_WhenGivenUnknownUuid() {
        Optional<OrderDB> actual = this.orderRepository.findByUuid(UUID.randomUUID());

        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    public void deleteByUuid_ShouldDeleteOrder_WhenGivenUuid() {
        this.orderRepository.deleteByUuid(this.expected.getUuid());

        Optional<OrderDB> actual = this.orderRepository.findById(this.expected.getId());

        assertThat(actual.isPresent()).isFalse();
    }
}
