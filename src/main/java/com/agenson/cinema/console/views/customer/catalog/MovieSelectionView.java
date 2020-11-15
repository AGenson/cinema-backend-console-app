package com.agenson.cinema.console.views.customer.catalog;

import com.agenson.cinema.console.template.AbstractStateView;
import com.agenson.cinema.console.template.RatioFormatter;
import com.agenson.cinema.console.views.identification.IdentificationView;
import com.agenson.cinema.room.RoomDTO;
import com.agenson.cinema.security.SecurityService;
import com.agenson.cinema.ticket.InvalidTicketException;
import com.agenson.cinema.ticket.TicketSeatDTO;
import com.agenson.cinema.ticket.seat.InvalidSeatException;
import com.agenson.cinema.ticket.seat.Seat;
import com.agenson.cinema.user.UserDetailsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MovieSelectionView extends AbstractStateView<RoomDTO> {

    private final SecurityService securityService;

    private final IdentificationView identificationView;

    private final ConfirmOrderView confirmOrderView;

    @Override
    protected void refreshState() {
        // IGNORED
    }

    @Override
    protected String getTitle() {
        return "Movie Selection";
    }

    @Override
    protected void printContent() {
        System.out.println("Room: " + this.state.getNumber());
        System.out.println("Movie: " + this.state.getMovie());
        System.out.println("Reserved: " + RatioFormatter.format(
                this.state.getTickets().size(),
                this.state.getCapacity()
        ));
        System.out.print("\n   ");

        for (int j = 1; j <= this.state.getNbCols(); j++)
            System.out.printf(" %02d", j);

        System.out.println("\n    " + String.join("", Collections.nCopies((3 * this.state.getNbCols() - 1), "-")));

        for (int i = 1; i <= this.state.getNbRows(); i++) {
            System.out.print(Seat.letterFromNumber(i) + " |");

            for (int j = 1; j <= this.state.getNbCols(); j++) {
                int row = i;
                int column = j;

                boolean isReserved = this.state.getTickets().stream()
                        .map(TicketSeatDTO::getSeat)
                        .filter(seat -> seat.getRow() == row && seat.getCol() == column)
                        .count() == 1;

                System.out.print("  " + (isReserved ? "R" : "A"));
            }

            System.out.println();
        }

        System.out.println("\nPlease select an action or seat:");
        System.out.println("[0] - Go back");
        System.out.println("[*] - Select a seat (e.g. A02)");
        System.out.println("[C] - Confirm order\n");
    }

    @Override
    protected void logic() {
        ArrayList<Seat> orderSeats = new ArrayList<>();
        boolean loop = true;

        this.setStayInView(false);
        this.setProcessInput(false);

        while (loop) {
            List<Seat> reservedSeats = this.state.getTickets().stream()
                    .map(TicketSeatDTO::getSeat)
                    .collect(Collectors.toList());

            reservedSeats.addAll(orderSeats);

            String input = this.getInput();

            if ("0".equals(input)) {
                return;
            } else if ("c".equals(input.toLowerCase())) {
                if (orderSeats.size() != 0) {
                    Optional<UserDetailsDTO> currentUser = this.securityService.getCurrentUser();

                    if (!currentUser.isPresent())
                        this.identificationView.handler();

                    currentUser = this.securityService.getCurrentUser();
                    loop = false;

                    currentUser.ifPresent(user -> {
                        this.confirmOrderView.handler(new OrderState(user, this.state, orderSeats));
                    });
                } else
                    System.out.println("Error: No seat has been selected");
            } else {
                try {
                    Seat seat = Seat.fromString(input.toUpperCase());

                    if (seat.getCol() > this.state.getNbCols() || seat.getRow() > this.state.getNbRows())
                        throw new InvalidTicketException(InvalidTicketException.Type.CAPACITY);
                    if (reservedSeats.stream().anyMatch(elt -> elt.equals(seat)))
                        throw new InvalidTicketException(InvalidTicketException.Type.EXISTS);
                    else
                        orderSeats.add(seat);
                } catch (InvalidTicketException | InvalidSeatException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }
    }
}
