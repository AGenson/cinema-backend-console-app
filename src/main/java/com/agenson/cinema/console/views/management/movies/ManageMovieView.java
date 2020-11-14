package com.agenson.cinema.console.views.management.movies;

import com.agenson.cinema.console.template.AbstractStateView;
import com.agenson.cinema.movie.MovieDTO;
import com.agenson.cinema.movie.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ManageMovieView extends AbstractStateView<MovieDTO> {

    private final MovieService movieService;

    private final EditMovieView editMovieView;

    @Override
    protected void refreshState() {
        this.state = this.movieService.findMovie(this.state.getUuid()).orElse(this.state);
    }

    @Override
    protected String getTitle() {
        return "Manage Movie";
    }

    @Override
    protected void printContent() {
        System.out.println("Title: " + this.state.getTitle() + "\n");

        System.out.println("Please select an action:");
        System.out.println("[0] - Go back");
        System.out.println("[1] - Edit Movie");
        System.out.println("[2] - Remove Movie\n");
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
                this.editMovieView.handler(this.state);
                this.refreshState();
                break;

            case "2":
                this.movieService.removeMovie(this.state.getUuid());
                this.setStayInView(false);
                break;

            default:
                this.informError();
                this.setProcessInput(true);
                break;
        }
    }
}
