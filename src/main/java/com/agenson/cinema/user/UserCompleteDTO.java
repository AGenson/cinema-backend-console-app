package com.agenson.cinema.user;

import com.agenson.cinema.order.OrderDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@EqualsAndHashCode(callSuper = true)
public class UserCompleteDTO extends UserDetailsDTO {

    private final List<OrderDTO> orders;

    public UserCompleteDTO(UserDB user) {
        super(user);
        this.orders = user.getOrders().stream()
                        .map(OrderDTO::new)
                        .collect(Collectors.toList());
    }
}
