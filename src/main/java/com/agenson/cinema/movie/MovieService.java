package com.agenson.cinema.movie;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    private final ModelMapper mapper;

    public Optional<MovieDTO> findMovie(UUID uuid) {
        return this.movieRepository.findByUuid(uuid).map(this::toDTO);
    }

    public Optional<MovieDTO> findMovie(String title) {
        return this.movieRepository.findByTitle(this.formatTitle(title)).map(this::toDTO);
    }

    public List<MovieDTO> findMovies() {
        return this.movieRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public MovieDTO createOrUpdateMovie(UUID uuid, String title) {
        MovieDB movie = this.movieRepository.findByUuid(uuid).orElse(new MovieDB());
        String formattedTitle = this.formatTitle(title);

        if (title == null) throw new InvalidMovieException(InvalidMovieException.Type.MANDATORY);
        else if (formattedTitle.length() == 0) throw new InvalidMovieException(InvalidMovieException.Type.MANDATORY);
        else if (formattedTitle.length() > 32) throw new InvalidMovieException(InvalidMovieException.Type.MAXSIZE);
        else {
            this.movieRepository.findByTitle(formattedTitle).ifPresent(movieWithSameTitle -> {
                if (movieWithSameTitle.getUuid() != movie.getUuid())
                    throw new InvalidMovieException(InvalidMovieException.Type.EXISTS);
            });
        }

        movie.setTitle(formattedTitle);

        return this.toDTO(this.movieRepository.save(movie));
    }

    public void removeMovie(UUID uuid) {
        this.movieRepository.deleteByUuid(uuid);
    }

    protected MovieDTO toDTO(MovieDB movie) {
        return this.mapper.map(movie, MovieDTO.class);
    }

    private String formatTitle(String title) {
        return title != null ? title.trim().toUpperCase() : null;
    }
}
