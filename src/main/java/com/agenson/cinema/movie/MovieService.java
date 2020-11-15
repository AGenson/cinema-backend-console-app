package com.agenson.cinema.movie;

import com.agenson.cinema.room.RoomService;
import com.agenson.cinema.security.restriction.RestrictToStaff;
import com.agenson.cinema.ticket.TicketDB;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    private final RoomService roomService;

    public Optional<MovieDTO> findMovie(UUID uuid) {
        return this.movieRepository.findByUuid(uuid).map(MovieDTO::new);
    }

    public Optional<MovieDTO> findMovie(String title) {
        return this.movieRepository.findByTitle(this.formatTitle(title)).map(MovieDTO::new);
    }

    public List<MovieDTO> findMovies() {
        return this.movieRepository.findAll().stream().map(MovieDTO::new).collect(Collectors.toList());
    }

    @RestrictToStaff
    public MovieDTO createMovie(String title) {
        this.validateTitle(null, title);

        return new MovieDTO(this.movieRepository.save(new MovieDB(this.formatTitle(title))));
    }

    @RestrictToStaff
    public Optional<MovieDTO> updateMovieTitle(UUID uuid, String title) {
        return this.movieRepository.findByUuid(uuid).map(movie -> {
            this.validateTitle(uuid, title);
            movie.setTitle(this.formatTitle(title));

            return new MovieDTO(this.movieRepository.save(movie));
        });
    }

    @RestrictToStaff
    public void removeMovie(UUID uuid) {
        this.movieRepository.findByUuid(uuid).ifPresent(movie -> {
            movie.getRooms().forEach(room -> this.roomService.updateRoomMovie(room.getUuid(), null));
            this.movieRepository.delete(movie);
        });
    }

    private void validateTitle(UUID uuid, String title) {
        String formattedTitle = this.formatTitle(title);

        if (formattedTitle == null) throw new InvalidMovieException(InvalidMovieException.Type.MANDATORY);
        else if (formattedTitle.length() == 0) throw new InvalidMovieException(InvalidMovieException.Type.MANDATORY);
        else if (formattedTitle.length() > 32) throw new InvalidMovieException(InvalidMovieException.Type.MAXSIZE);
        else {
            this.movieRepository.findByTitle(formattedTitle).ifPresent(movieWithSameTitle -> {
                if (uuid == null || movieWithSameTitle.getUuid() != uuid)
                    throw new InvalidMovieException(InvalidMovieException.Type.EXISTS);
            });
        }
    }

    private String formatTitle(String title) {
        return title != null ? title.trim().toUpperCase() : null;
    }
}
