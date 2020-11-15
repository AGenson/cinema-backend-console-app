package com.agenson.cinema.console.views.customer.catalog;

import com.agenson.cinema.console.template.AbstractStateView;
import com.agenson.cinema.order.OrderDTO;
import com.agenson.cinema.order.OrderService;
import com.agenson.cinema.ticket.InvalidTicketException;
import com.agenson.cinema.ticket.TicketService;
import com.agenson.cinema.ticket.seat.Seat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ConfirmOrderView extends AbstractStateView<OrderState> {

    private final OrderService orderService;

    private final TicketService ticketService;

    private final OrderReviewView orderReviewView;

    @Override
    protected void refreshState() {
        // IGNORED
    }

    @Override
    protected String getTitle() {
        return "Confirm Order";
    }

    @Override
    protected void printContent() {
        System.out.println("Room: " + this.state.getRoom().getNumber());
        System.out.println("Movie: " + this.state.getRoom().getMovie());
        System.out.println("Seats: " + this.state.getSeats().stream()
                .map(Seat::toString).collect(Collectors.joining(", ")));

        System.out.println("Price ($): " + this.state.getSeats().stream()
                .map(seat -> this.state.getRoom().getPrice(seat)).reduce(0, Integer::sum) + "\n");
    }

    @Override
    protected void logic() {
        this.setStayInView(false);
        this.setProcessInput(false);

        if (this.ask("Confirm?")) {
            OrderDTO order = this.orderService.createOrder(this.state.getUser().getUuid());
            ArrayList<String> ticketsStatus = new ArrayList<>();

            for (Seat seat : this.state.getSeats()) {
                String status = "Seat " + seat + " -> ";

                try {
                    this.ticketService.createTicket(this.state.getRoom().getUuid(), order.getUuid(), seat);
                    status += "Ordered";
                } catch (InvalidTicketException ex) {
                    status += ex.getMessage();
                }

                ticketsStatus.add(status);
            }

            this.orderReviewView.handler(new ReviewState(this.state.getRoom(), ticketsStatus));
        }
    }
}
