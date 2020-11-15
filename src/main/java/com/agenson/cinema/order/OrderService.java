package com.agenson.cinema.order;

import com.agenson.cinema.security.restriction.RestrictToUser;
import com.agenson.cinema.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final UserRepository userRepository;

    @RestrictToUser(argName = "userUuid")
    public OrderDTO createOrder(UUID userUuid) {
        return this.userRepository.findByUuid(userUuid).map(user -> {
            OrderDB order = this.orderRepository.save(new OrderDB(user));

            return new OrderDTO(order);
        }).orElseThrow(() -> new InvalidOrderException(InvalidOrderException.Type.USER));
    }
}
