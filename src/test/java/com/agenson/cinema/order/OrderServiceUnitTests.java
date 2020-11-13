package com.agenson.cinema.order;

import com.agenson.cinema.user.UserDB;
import com.agenson.cinema.user.UserDTO;
import com.agenson.cinema.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
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
public class OrderServiceUnitTests {

    private static final String ENCODED_PASSWORD = new BCryptPasswordEncoder().encode("password");

    private static final UserDB DEFAULT_USER = new UserDB("username", ENCODED_PASSWORD);

    @Mock
    private ModelMapper mapper;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    public void setup() {
        lenient().when(this.mapper.map(any(OrderDB.class), ArgumentMatchers.<Class<OrderDTO>>any()))
                .thenAnswer(invocation -> {
                    OrderDB order = invocation.getArgument(0);
                    UserDB user = order.getUser();

                    return new OrderDTO(order.getUuid(), new UserDTO(user.getUuid(), user.getUsername()));
                });
    }

    @Test
    public void findOrder_ShouldReturnOrder_WhenGivenUuid() {
        OrderDB order = new OrderDB(DEFAULT_USER);

        when(this.orderRepository.findByUuid(order.getUuid())).thenReturn(Optional.of(order));

        OrderDTO expected = this.mapper.map(order, OrderDTO.class);
        Optional<OrderDTO> actual = this.orderService.findOrder(order.getUuid());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void findOrder_ShouldReturnNull_WhenGivenUnknownUuid() {
        when(this.orderRepository.findByUuid(any(UUID.class))).thenReturn(Optional.empty());

        assertThat(this.orderService.findOrder(UUID.randomUUID()).isPresent()).isFalse();
        assertThat(this.orderService.findOrder(null).isPresent()).isFalse();
    }

    @Test
    public void findOrders_ShouldReturnOrderList() {
        List<OrderDB> orderList = Arrays.asList(
                new OrderDB(DEFAULT_USER),
                new OrderDB(DEFAULT_USER));

        when(this.orderRepository.findAll()).thenReturn(orderList);

        List<OrderDTO> actual = this.orderService.findOrders();
        List<OrderDTO> expected = orderList.stream()
                .map(order -> this.mapper.map(order, OrderDTO.class))
                .collect(Collectors.toList());

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsOnlyOnceElementsOf(expected);
    }

    @Test
    public void findOrders_ShouldReturnFilteredOrderList_WhenGivenUserUuid() {
        UserDB user1 = new UserDB("username", ENCODED_PASSWORD);
        UserDB user2 = new UserDB("another", ENCODED_PASSWORD);

        OrderDB order1 = new OrderDB(user1);
        OrderDB order2 = new OrderDB(user2);
        List<OrderDB> orderList = Arrays.asList(order1, order2);

        user1.setOrders(orderList.stream().filter(order -> order.getUser().equals(user1)).collect(Collectors.toList()));
        when(this.userRepository.findByUuid(user1.getUuid())).thenReturn(Optional.of(user1));

        List<OrderDTO> actual = this.orderService.findOrders(user1.getUuid());
        List<OrderDTO> expected = Collections.singletonList(this.mapper.map(order1, OrderDTO.class));

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsOnlyOnceElementsOf(expected);
    }

    @Test
    public void createOrder_ShouldReturnNewOrder_WhenGivenUserUuid() {
        when(this.orderRepository.save(any(OrderDB.class))).then(returnsFirstArg());
        when(this.userRepository.findByUuid(DEFAULT_USER.getUuid())).thenReturn(Optional.of(DEFAULT_USER));

        OrderDTO actual = this.orderService.createOrder(DEFAULT_USER.getUuid());

        assertThat(actual.getUuid()).isNotNull();
        assertThat(actual.getUser()).isEqualTo(new UserDTO(DEFAULT_USER.getUuid(), DEFAULT_USER.getUsername()));
    }

    @Test
    public void createOrder_ShouldThrowInvalidOrderException_WhenGivenInvalidUserUuid() {
        when(this.userRepository.findByUuid(any(UUID.class))).thenReturn(Optional.empty());
        when(this.userRepository.findByUuid(null)).thenReturn(Optional.empty());

        for (UUID uuid : Arrays.asList(null, UUID.randomUUID()))
            assertThatExceptionOfType(InvalidOrderException.class)
                    .isThrownBy(() -> this.orderService.createOrder(uuid))
                    .withMessage(InvalidOrderException.Type.USER.toString());
    }
}
