package com.agenson.cinema;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class ConsoleIO implements CommandLineRunner {

    @Override
    public void run(String... args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Hello World!");

        System.out.print("Input: ");
        System.out.println("Output: " + sc.nextLine());
    }
}
