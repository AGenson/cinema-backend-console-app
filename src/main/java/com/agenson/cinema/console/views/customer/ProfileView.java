package com.agenson.cinema.console.views.customer;

import com.agenson.cinema.console.template.AbstractStatelessView;
import com.agenson.cinema.order.OrderDTO;
import com.agenson.cinema.room.RoomDTO;
import com.agenson.cinema.security.SecurityRole;
import com.agenson.cinema.security.SecurityService;
import com.agenson.cinema.ticket.TicketSeatDTO;
import com.agenson.cinema.ticket.seat.Seat;
import com.agenson.cinema.user.UserCompleteDTO;
import com.agenson.cinema.user.UserDetailsDTO;
import com.agenson.cinema.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProfileView extends AbstractStatelessView {

    private final SecurityService securityService;

    private final UserService userService;

    @Override
    protected String getTitle() {
        return "Profile";
    }

    @Override
    protected void printContent() {
        Optional<UserDetailsDTO> userDetails = this.securityService.getCurrentUser();

        if (userDetails.isPresent()) {
            Optional<UserCompleteDTO> user = this.userService.findUser(userDetails.get().getUuid());

            if (user.isPresent()) {
                System.out.println("Username: " + user.get().getUsername());
                System.out.println("Role: " + (SecurityRole.STAFF.equals(user.get().getRole()) ? "STAFF" : "CUSTOMER"));

                if (user.get().getOrders().size() != 0) {
                    System.out.println("Orders:");

                    for (OrderDTO order : user.get().getOrders()) {
                        if (order.getTickets().size() != 0) {
                            RoomDTO room = order.getTickets().get(0).getRoom();
                            List<Seat> seats = order.getTickets().stream().map(TicketSeatDTO::getSeat)
                                    .collect(Collectors.toList());

                            System.out.println("\n> Room: " + room.getNumber());
                            System.out.println("  Movie: " + room.getMovie());
                            System.out.println("  Seats: " + seats.stream().map(Seat::toString)
                                    .collect(Collectors.joining(", ")));
                            System.out.println("  Price ($): " + seats.stream().map(room::getPrice)
                                    .reduce(0, Integer::sum));
                        }
                    }
                } else
                    System.out.println("Orders: none");
            }
        }
        System.out.println("\nEnter anything to continue.");
    }

    @Override
    protected void logic() {
        this.setStayInView(false);
        this.setProcessInput(false);

        this.getInput();
    }
}
