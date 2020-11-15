package com.agenson.cinema.console.views.customer.catalog;

import com.agenson.cinema.console.template.AbstractListView;
import com.agenson.cinema.console.template.RatioFormatter;
import com.agenson.cinema.room.RoomDTO;
import com.agenson.cinema.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MovieCatalogView extends AbstractListView<RoomDTO> {

    private static final int OFFSET = 1;

    private final RoomService roomService;

    private final MovieSelectionView movieSelectionView;

    @Override
    protected void refreshList() {
        this.list = this.roomService.findRooms();

        this.list.removeIf(room -> room.getMovie() == null);
    }

    @Override
    protected String getTitle() {
        return "Movie Catalog";
    }

    @Override
    protected void printContent() {
        System.out.println("Please select an action or movie:");
        System.out.println("[0] - Go back");

        for (int i = 0; i < this.list.size(); i++) {
            RoomDTO room = this.list.get(i);

            System.out.println("\n[" + (i + OFFSET) + "] - Room: " + room.getNumber());
            System.out.println("    > Movie: " + room.getMovie().getTitle());
            System.out.println("    > Reserved: " + RatioFormatter.format(
                    room.getTickets().size(),
                    room.getCapacity()
            ));
        }

        System.out.println();
    }

    public void logic() {
        this.setProcessInput(false);

        String input = this.getInput();

        try {
            int value = Integer.parseInt(input);

            if (value == 0)
                this.setStayInView(false);
            else if (value >= OFFSET && value < this.list.size() + OFFSET) {
                this.movieSelectionView.handler(this.list.get(value - OFFSET));
                this.refreshList();
            } else
                throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            this.informError();
            this.setProcessInput(true);
        }
    }
}
