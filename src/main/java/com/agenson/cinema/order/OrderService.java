package com.agenson.cinema.order;

import com.agenson.cinema.security.RestrictToStaff;
import com.agenson.cinema.security.RestrictToUser;
import com.agenson.cinema.user.UserDB;
import com.agenson.cinema.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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

    private final ModelMapper mapper;

    public Optional<OrderDTO> findOrder(UUID uuid) {
        return this.orderRepository.findByUuid(uuid).map(this::toDTO);
    }

    @RestrictToStaff
    public List<OrderDTO> findOrders() {
        return this.orderRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @RestrictToUser(argName = "userUuid")
    public List<OrderDTO> findOrders(UUID userUuid) {
        return this.userRepository.findByUuid(userUuid).map(UserDB::getOrders)
                .map(orders -> orders.stream().map(this::toDTO).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @RestrictToStaff
    public OrderDTO createOrder(UUID userUuid) {
        return this.userRepository.findByUuid(userUuid).map(user -> {
            OrderDB order = this.orderRepository.save(new OrderDB(user));

            return this.toDTO(order);
        }).orElseThrow(() -> new InvalidOrderException(InvalidOrderException.Type.USER));
    }

    @RestrictToStaff
    public void removeOrder(UUID uuid) {
        this.orderRepository.deleteByUuid(uuid);
    }

    private OrderDTO toDTO(OrderDB order) {
        return this.mapper.map(order, OrderDTO.class);
    }
}
