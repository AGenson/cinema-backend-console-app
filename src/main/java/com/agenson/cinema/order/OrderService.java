package com.agenson.cinema.order;

import com.agenson.cinema.security.restriction.RestrictToStaff;
import com.agenson.cinema.security.restriction.RestrictToUser;
import com.agenson.cinema.user.UserDB;
import com.agenson.cinema.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final UserRepository userRepository;

    public Optional<OrderDTO> findOrder(UUID uuid) {
        return this.orderRepository.findByUuid(uuid).map(OrderDTO::new);
    }

    @RestrictToStaff
    public List<OrderDTO> findOrders() {
        return this.orderRepository.findAll().stream().map(OrderDTO::new).collect(Collectors.toList());
    }

    @RestrictToUser(argName = "userUuid")
    public List<OrderDTO> findOrders(UUID userUuid) {
        return this.userRepository.findByUuid(userUuid).map(UserDB::getOrders)
                .map(orders -> orders.stream().map(OrderDTO::new).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @RestrictToStaff
    public OrderDTO createOrder(UUID userUuid) {
        return this.userRepository.findByUuid(userUuid).map(user -> {
            OrderDB order = this.orderRepository.save(new OrderDB(user));

            return new OrderDTO(order);
        }).orElseThrow(() -> new InvalidOrderException(InvalidOrderException.Type.USER));
    }

    @RestrictToStaff
    public void removeOrder(UUID uuid) {
        this.orderRepository.deleteByUuid(uuid);
    }
}
