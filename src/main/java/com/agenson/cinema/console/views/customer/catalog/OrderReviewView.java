package com.agenson.cinema.console.views.customer.catalog;

import com.agenson.cinema.console.template.AbstractStateView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderReviewView extends AbstractStateView<ReviewState> {

    @Override
    protected void refreshState() {
        // IGNORED
    }

    @Override
    protected String getTitle() {
        return "Order Review";
    }

    @Override
    protected void printContent() {
        System.out.println("Room: " + this.state.getRoom().getNumber());
        System.out.println("Movie: " + this.state.getRoom().getMovie());
        this.state.getTicketsStatus().forEach(System.out::println);
        System.out.println("\nEnter anything to continue.");
    }

    @Override
    protected void logic() {
        this.setStayInView(false);
        this.setProcessInput(false);

        this.getInput();
    }
}
