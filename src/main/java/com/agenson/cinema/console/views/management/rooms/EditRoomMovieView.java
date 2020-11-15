package com.agenson.cinema.console.views.management.rooms;

import com.agenson.cinema.console.template.AbstractStateView;
import com.agenson.cinema.movie.MovieDTO;
import com.agenson.cinema.movie.MovieService;
import com.agenson.cinema.room.RoomDTO;
import com.agenson.cinema.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EditRoomMovieView extends AbstractStateView<RoomDTO> {

    private static final int OFFSET = 1;

    private final RoomService roomService;

    private final MovieService movieService;

    private List<MovieDTO> list = Collections.emptyList();

    @Override
    protected void refreshState() {
        // IGNORED
    }

    @Override
    protected String getTitle() {
        return "Edit Room Movie";
    }

    @Override
    protected void printContent() {
        this.list = this.movieService.findMovies();

        System.out.println("(Selecting a new movie will delete all tickets for the room)\n");

        System.out.println("Please select an action or movie:");
        System.out.println("[0] - Go back\n");

        for (int i = 0; i < list.size(); i++)
            System.out.println("[" + (i+OFFSET) + "] - " + list.get(i).getTitle());

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
            else if (value >= OFFSET && value < this.list.size() + OFFSET) {
                this.roomService.updateRoomMovie(this.state.getUuid(), this.list.get(value - OFFSET).getUuid());
                this.setStayInView(false);
            } else
                throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            this.informError();
            this.setProcessInput(true);
        }
    }
}
