package com.agenson.cinema.console.views.management.movies;

import com.agenson.cinema.console.template.AbstractStateView;
import com.agenson.cinema.movie.InvalidMovieException;
import com.agenson.cinema.movie.MovieDTO;
import com.agenson.cinema.movie.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EditMovieTitleView extends AbstractStateView<MovieDTO> {

    private final MovieService movieService;

    @Override
    protected void refreshState() {
        // IGNORED
    }

    @Override
    protected String getTitle() {
        return "Edit Movie Title";
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
                this.movieService.updateMovieTitle(this.state.getUuid(), title);
                this.setStayInView(false);
            } catch (InvalidMovieException ex) {
                System.out.println("\n" + ex.getMessage());
                this.setProcessInput(true);
            }
        }
    }
}
