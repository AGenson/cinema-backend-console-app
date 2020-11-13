package com.agenson.cinema.order;

import com.agenson.cinema.user.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    private UUID uuid;
    private UserDTO user;
}
