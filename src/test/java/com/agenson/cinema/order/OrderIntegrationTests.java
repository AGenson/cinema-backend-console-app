package com.agenson.cinema.order;

import com.agenson.cinema.security.SecurityContext;
import com.agenson.cinema.security.UserRole;
import com.agenson.cinema.user.UserDB;
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
public class OrderIntegrationTests {

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private SecurityContext securityContext;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    private UserDB defaultUser;

    private UserDB anotherUser;

    @BeforeEach
    public void setup() {
        String encodedPassword = new BCryptPasswordEncoder().encode("password");
        UserDB user1 = new UserDB("username", encodedPassword);
        UserDB user2 = new UserDB("another", encodedPassword);

        user1.setRole(UserRole.STAFF);
        this.entityManager.persist(user1);
        this.defaultUser = user1;

        this.entityManager.persist(user2);
        this.anotherUser = user2;

        this.loginAs(this.defaultUser.getUuid(), this.defaultUser.getRole());
    }

    @AfterEach
    public void logout() {
        this.securityContext.logout();
    }

    @Test
    public void findOrder_ShouldReturnPersistedRoom_WhenGivenUuid() {
        OrderDB order = this.orderRepository.save(new OrderDB(this.defaultUser));

        OrderDTO expected = this.mapper.map(order, OrderDTO.class);
        Optional<OrderDTO> actual = this.orderService.findOrder(order.getUuid());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void findOrder_ShouldReturnNull_WhenNotFoundWithUuid() {
        assertThat(this.orderService.findOrder(UUID.randomUUID()).isPresent()).isFalse();
    }

    @Test
    public void findOrders_ShouldReturnOrderList() {
        List<OrderDB> orderList = Arrays.asList(
                new OrderDB(this.defaultUser),
                new OrderDB(this.defaultUser)
        );

        assertThat(this.orderRepository.findAll().size()).isZero();

        this.orderRepository.saveAll(orderList);

        List<OrderDTO> actual = this.orderService.findOrders();
        List<OrderDTO> expected = orderList.stream()
                .map(order -> this.mapper.map(order, OrderDTO.class))
                .collect(Collectors.toList());

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsOnlyOnceElementsOf(expected);
    }

    @Test
    public void findOrders_ShouldThrowSecurityException_WhenNotLoggedInAsStaff() {
        StaffSecurityAssertion.assertShouldThrowSecurityException(
                () -> this.orderService.findOrders(),
                () -> this.loginAs(null, UserRole.CUSTOMER),
                () -> this.logout()
        );
    }

    @Test
    public void findOrders_ShouldReturnFilteredRoomList_WhenGivenUserUuid() {
        OrderDB order1 = new OrderDB(this.defaultUser);
        OrderDB order2 = new OrderDB(this.anotherUser);

        this.orderRepository.saveAll(Arrays.asList(order1, order2));
        this.entityManager.refresh(this.defaultUser);

        List<OrderDTO> actual = this.orderService.findOrders(this.defaultUser.getUuid());
        List<OrderDTO> expected = Collections.singletonList(this.mapper.map(order1, OrderDTO.class));

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsOnlyOnceElementsOf(expected);
    }

    @Test
    public void findOrders_ShouldThrowSecurityException_WhenNotCalledByUser() {
        StaffSecurityAssertion.assertShouldThrowSecurityException(
                () -> this.orderService.findOrders(this.defaultUser.getUuid()),
                () -> this.loginAs(null, UserRole.STAFF),
                () -> this.logout()
        );
    }

    @Test
    public void createOrder_ShouldReturnPersistedOrder_WhenGivenUserUuid() {
        OrderDTO expected = this.orderService.createOrder(this.defaultUser.getUuid());
        Optional<OrderDTO> actual = this.orderRepository.findByUuid(expected.getUuid())
                .map(order -> this.mapper.map(order, OrderDTO.class));

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void createOrder_ShouldNotPersistOrder_WhenGivenInvalidUserUuid() {
        this.orderRepository.save(new OrderDB(this.defaultUser));

        List<OrderDB> expected = this.orderRepository.findAll();

        assertThatExceptionOfType(InvalidOrderException.class)
                .isThrownBy(() -> this.orderService.createOrder(UUID.randomUUID()));

        List<OrderDB> actual = this.orderRepository.findAll();

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsOnlyOnceElementsOf(expected);
    }

    @Test
    public void createOrder_ShouldThrowSecurityException_WhenNotLoggedInAsStaff() {
        StaffSecurityAssertion.assertShouldThrowSecurityException(
                () -> this.orderService.createOrder(UUID.randomUUID()),
                () -> this.loginAs(null, UserRole.CUSTOMER),
                () -> this.logout()
        );
    }

    @Test
    public void removeOrder_ShouldRemoveOrder_WhenGivenUuid() {
        OrderDB order = this.orderRepository.save(new OrderDB(this.defaultUser));

        this.orderService.removeOrder(order.getUuid());
        Optional<OrderDB> actual = this.orderRepository.findByUuid(order.getUuid());

        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    public void removeOrder_ShouldThrowSecurityException_WhenNotLoggedInAsStaff() {
        StaffSecurityAssertion.assertShouldThrowSecurityException(
                () -> this.orderService.removeOrder(UUID.randomUUID()),
                () -> this.loginAs(null, UserRole.CUSTOMER),
                () -> this.logout()
        );
    }

    private void loginAs(UUID uuid, UserRole role) {
        UUID newUUID = uuid != null ? uuid : UUID.randomUUID();

        this.securityContext.login(newUUID, role);
    }
}
