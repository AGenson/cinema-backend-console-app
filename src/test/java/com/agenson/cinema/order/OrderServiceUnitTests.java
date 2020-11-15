package com.agenson.cinema.order;

import com.agenson.cinema.user.UserDB;
import com.agenson.cinema.user.UserRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceUnitTests {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    public void createOrder_ShouldReturnNewOrder_WhenGivenUserUuid() {
        String encodedPassword = new BCryptPasswordEncoder().encode("password");
        UserDB user = new UserDB("username", encodedPassword);

        when(this.orderRepository.save(any(OrderDB.class))).then(returnsFirstArg());
        when(this.userRepository.findByUuid(user.getUuid())).thenReturn(Optional.of(user));

        OrderDTO actual = this.orderService.createOrder(user.getUuid());

        assertThat(actual.getUuid()).isNotNull();
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
