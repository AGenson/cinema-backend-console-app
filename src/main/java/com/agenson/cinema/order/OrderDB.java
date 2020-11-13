package com.agenson.cinema.order;

import com.agenson.cinema.user.UserDB;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "\"order\"")
public class OrderDB {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id = -1L;
    private UUID uuid = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id")
    private UserDB user;

    public OrderDB(UserDB user) {
        this.user = user;
    }
}
