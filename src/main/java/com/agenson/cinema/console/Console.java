package com.agenson.cinema.console;

import com.agenson.cinema.console.views.MainMenuView;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Profile("!test")
@Component
public class Console implements CommandLineRunner {

    private final MainMenuView mainMenuView;

    private static void welcome() {
        System.out.println("########################");
        System.out.println("Welcome to the Cinema!");
    }

    private static void goodbye() {
        System.out.println("\n########################");
        System.out.println("Thank you! See you soon!");
    }

    @Override
    public void run(String... args) {
        Console.welcome();
        mainMenuView.handler();
        Console.goodbye();
    }
}
