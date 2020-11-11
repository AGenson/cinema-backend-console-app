package com.agenson.cinema.user;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
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
    private Role role = Role.COSTUMER;

    public UserDB(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
