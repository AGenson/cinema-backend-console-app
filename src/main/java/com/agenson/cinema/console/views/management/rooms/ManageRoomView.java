package com.agenson.cinema.console.views.management.rooms;

import com.agenson.cinema.console.template.AbstractStateView;
import com.agenson.cinema.movie.MovieDTO;
import com.agenson.cinema.room.RoomDTO;
import com.agenson.cinema.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ManageRoomView extends AbstractStateView<RoomDTO> {

    private final RoomService roomService;

    private final EditRoomMovieView editRoomMovieView;

    @Override
    protected void refreshState() {
        this.state = this.roomService.findRoom(this.state.getUuid()).orElse(this.state);
    }

    @Override
    protected String getTitle() {
        return "Manage Room";
    }

    @Override
    protected void printContent() {
        MovieDTO movie = this.state.getMovie();

        System.out.println("Room: " + this.state.getNumber());
        System.out.println("Capacity: " + this.state.getCapacity());
        System.out.println("Movie: " + (movie != null ? movie : "-") + "\n");

        System.out.println("Please select an action:");
        System.out.println("[0] - Go back");
        System.out.println("[1] - Edit Movie");
        System.out.println("[2] - Remove Room\n");
    }

    @Override
    protected void logic() {
        this.setProcessInput(false);

        String input = this.getInput();

        switch (input) {

            case "0":
                this.setStayInView(false);;
                break;

            case "1":
                this.editRoomMovieView.handler(this.state);
                this.refreshState();
                break;

            case "2":
                this.roomService.removeRoom(this.state.getUuid());
                this.setStayInView(false);
                break;

            default:
                this.informError();
                this.setProcessInput(true);
                break;
        }
    }
}
