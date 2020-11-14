package com.agenson.cinema;

import com.agenson.cinema.movie.MovieDTO;
import com.agenson.cinema.movie.MovieService;
import com.agenson.cinema.order.OrderDTO;
import com.agenson.cinema.order.OrderService;
import com.agenson.cinema.room.RoomDTO;
import com.agenson.cinema.room.RoomService;
import com.agenson.cinema.ticket.TicketDTO;
import com.agenson.cinema.ticket.TicketService;
import com.agenson.cinema.user.UserDTO;
import com.agenson.cinema.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@RequiredArgsConstructor
@Profile("!test")
@Component
public class ConsoleIO implements CommandLineRunner {

    private final MovieService movieService;

    private final RoomService roomService;

    private final UserService userService;

    private final OrderService orderService;

    private final TicketService ticketService;

    @Override
    public void run(String... args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Hello World!");

        System.out.print("\nInput: ");
        System.out.println("Output: " + sc.nextLine());

        System.out.println("\nMovies:");

        for (MovieDTO movie : movieService.findMovies())
            System.out.println(movie);

        System.out.println("\nRooms:");

        for (RoomDTO room : roomService.findRooms())
            System.out.println(room);

        this.userService.loginUser("staff", "password");

        System.out.println("\nUsers:");

        for (UserDTO user : userService.findUsers())
            System.out.println(user);

        System.out.println("\nOrders:");

        for (OrderDTO order : orderService.findOrders())
            System.out.println(order);

        System.out.println("\nTickets:");

        for (TicketDTO ticket : ticketService.findTickets())
            System.out.println(ticket);
    }
}
