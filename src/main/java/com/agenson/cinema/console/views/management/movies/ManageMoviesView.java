package com.agenson.cinema.console.views.management.movies;

import com.agenson.cinema.console.template.AbstractStatelessView;
import com.agenson.cinema.movie.MovieDTO;
import com.agenson.cinema.movie.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ManageMoviesView extends AbstractStatelessView {

    private static final int OFFSET = 2;

    private final MovieService movieService;

    private final NewMovieView newMovieView;

    private final ManageMovieView manageMovieView;

    private List<MovieDTO> list = Collections.emptyList();

    private void refreshList() {
        this.list = this.movieService.findMovies();
    }

    @Override
    protected String getTitle() {
        return "Manage Movies";
    }

    @Override
    protected void printContent() {
        System.out.println("Please select an action or movie:");
        System.out.println("[0] - Go back");
        System.out.println("[1] - Add a movie\n");

        for (int i = 0; i < this.list.size(); i++)
            System.out.println("[" + (i+OFFSET) + "] - " + this.list.get(i).getTitle());

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
                this.newMovieView.handler();
                this.refreshList();
            } else if (value >= OFFSET && value < this.list.size() + OFFSET) {
                this.manageMovieView.handler(this.list.get(value - OFFSET));
                this.refreshList();
            } else
                throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            this.informError();
            this.setProcessInput(true);
        }
    }

    @Override
    public void handler() {
        this.refreshList();
        super.handler();
    }
}
