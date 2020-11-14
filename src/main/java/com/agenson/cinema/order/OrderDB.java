package com.agenson.cinema.order;

import com.agenson.cinema.ticket.TicketDB;
import com.agenson.cinema.user.UserDB;
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
@Table(name = "\"order\"")
public class OrderDB {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id = -1L;
    private UUID uuid = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id")
    private UserDB user;

    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "order")
    private List<TicketDB> tickets = Collections.emptyList();

    public OrderDB(UserDB user) {
        this.user = user;
    }
}
