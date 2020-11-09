package com.agenson.cinema;

import com.agenson.cinema.movie.MovieDTO;
import com.agenson.cinema.movie.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@RequiredArgsConstructor
@Profile("!test")
@Component
public class ConsoleIO implements CommandLineRunner {

    private final MovieService movieService;

    @Override
    public void run(String... args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Hello World!");

        System.out.print("\nInput: ");
        System.out.println("Output: " + sc.nextLine());

        System.out.println("\nMovies:");

        for (MovieDTO movie : movieService.findMovies())
            System.out.println(movie);
    }
}
