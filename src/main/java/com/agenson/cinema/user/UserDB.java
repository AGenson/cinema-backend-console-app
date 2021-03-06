package com.agenson.cinema.user;

import com.agenson.cinema.order.OrderDB;
import com.agenson.cinema.security.SecurityRole;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "\"user\"")
public class UserDB {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id = -1L;
    private UUID uuid = UUID.randomUUID();
    private String username = "";
    private String password = "";

    @Enumerated(EnumType.ORDINAL)
    private SecurityRole role = SecurityRole.CUSTOMER;

    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<OrderDB> orders = Collections.emptyList();

    public UserDB(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
