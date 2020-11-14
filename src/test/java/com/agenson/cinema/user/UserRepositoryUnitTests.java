package com.agenson.cinema.user;

import com.agenson.cinema.order.OrderDB;
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
public class UserRepositoryUnitTests implements UserConstants {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private UserDB expected;

    @BeforeEach
    public void setup() {
        expected = this.entityManager.persist(new UserDB(NORMAL_USERNAME, ENCODER.encode(NORMAL_PASSWORD)));
    }

    @Test
    public void findByUuid_ShouldReturnUser_WhenGivenPersistedUuid() {
        Optional<UserDB> actual = this.userRepository.findByUuid(this.expected.getUuid());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(this.expected);
    }

    @Test
    public void findByUuid_ShouldReturnNull_WhenGivenUnknownUuid() {
        Optional<UserDB> actual = this.userRepository.findByUuid(UUID.randomUUID());

        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    public void findByUsername_ShouldReturnMovie_WhenGivenPersistedUsername() {
        Optional<UserDB> actual = this.userRepository.findByUsername(this.expected.getUsername());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(this.expected);
    }

    @Test
    public void findByUsername_ShouldReturnNull_WhenGivenUnknownUsername() {
        Optional<UserDB> actual = this.userRepository.findByUsername(UNKNOWN_USERNAME);

        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    public void deleteByUuid_ShouldDeleteUser_WhenGivenUuid() {
        this.userRepository.deleteByUuid(this.expected.getUuid());

        Optional<UserDB> actual = this.userRepository.findById(this.expected.getId());

        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    public void deleteByUuid_ShouldDeleteOrders_WhenGiven_Uuid() {
        OrderDB order = new OrderDB(this.expected);

        order = this.entityManager.persist(order);

        this.entityManager.refresh(this.expected);
        this.entityManager.refresh(order);

        assertThat(this.entityManager.find(OrderDB.class, order.getId())).isNotNull();
        assertThat(this.expected.getOrders()).containsOnly(order);
        assertThat(order.getUser()).isEqualTo(this.expected);

        this.userRepository.deleteByUuid(this.expected.getUuid());
        this.entityManager.flush();

        assertThat(this.entityManager.find(OrderDB.class, order.getId())).isNull();
    }
}
