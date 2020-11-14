package com.agenson.cinema.console.views.management.movies;

import com.agenson.cinema.console.template.AbstractStatelessView;
import com.agenson.cinema.movie.InvalidMovieException;
import com.agenson.cinema.movie.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NewMovieView extends AbstractStatelessView {

    private final MovieService movieService;

    @Override
    protected String getTitle() {
        return "New Movie";
    }

    @Override
    protected void printContent() {
        this.informCancel();
    }

    @Override
    protected void logic() {
        this.setProcessInput(false);

        String title = this.getInput("Title");

        if ("c".equals(title.toLowerCase()))
            this.setStayInView(false);
        else {
            try {
                this.movieService.createMovie(title);
                this.setStayInView(false);
            } catch (InvalidMovieException ex) {
                System.out.println(ex.getMessage());
                this.setProcessInput(true);
            }
        }
    }
}
