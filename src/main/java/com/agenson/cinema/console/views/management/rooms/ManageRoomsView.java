package com.agenson.cinema.console.views.management.rooms;

import com.agenson.cinema.console.template.AbstractListView;
import com.agenson.cinema.console.template.RatioFormatter;
import com.agenson.cinema.room.RoomDTO;
import com.agenson.cinema.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ManageRoomsView extends AbstractListView<RoomDTO> {

    private static final int OFFSET = 2;

    private final RoomService roomService;

    private final NewRoomView newRoomView;

    private final ManageRoomView manageRoomView;

    @Override
    protected void refreshList() {
        this.list = this.roomService.findRooms();
    }

    @Override
    protected String getTitle() {
        return "Manage Rooms";
    }

    @Override
    protected void printContent() {
        System.out.println("Please select an action or room:");
        System.out.println("[0] - Go back");
        System.out.println("[1] - Add a room");

        for (int i = 0; i < this.list.size(); i++) {
            RoomDTO room = this.list.get(i);

            System.out.println("\n[" + (i + OFFSET) + "] - Room: " + room.getNumber());

            if (room.getMovie() != null) {
                System.out.println("    > Movie: " + room.getMovie());
                System.out.println("    > Reserved: " + RatioFormatter.format(
                        room.getTickets().size(),
                        room.getCapacity()
                ));
                System.out.println("    > Income ($): " + RatioFormatter.format(
                        room.getIncome(),
                        room.getPotentialIncome()
                ));
            } else
                System.out.println("    > Capacity: " + room.getCapacity());
        }

        System.out.println();
    }

    @Override
    protected void logic() {
        this.setProcessInput(false);

        String input = this.getInput();

        try {
            int value = Integer.parseInt(input);

            if (value == 0)
                this.setStayInView(false);
            else if (value == 1) {
                this.newRoomView.handler();
                this.refreshList();
            } else if (value >= OFFSET && value < this.list.size() + OFFSET) {
                this.manageRoomView.handler(this.list.get(value - OFFSET));
                this.refreshList();
            } else
                throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            this.informError();
            this.setProcessInput(true);
        }
    }
}
