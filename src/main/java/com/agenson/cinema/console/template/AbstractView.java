package com.agenson.cinema.console.template;

import lombok.Setter;

import java.util.Scanner;

public abstract class AbstractView {

    private static final Scanner SCANNER = new Scanner(System.in);

    @Setter
    private boolean stayInView = true;

    @Setter
    private boolean processInput = true;

    protected abstract String getTitle();

    protected String getInput(String field) {
        System.out.print(field + " = ");

        return SCANNER.nextLine();
    }

    protected String getInput() {
        return this.getInput("Input");
    }

    protected void askRetry() {
        System.out.println("Retry? (y/yes or n/no)");

        String input = this.getInput().toLowerCase();

        if ("y".equals(input) || "yes".equals(input))
            this.setStayInView(true);
    }

    protected void informError() {
        System.out.println("Error: Input incorrect");
    }

    protected void informCancel() {
        System.out.println("Enter 'C' to cancel.\n");
    }

    private void printHeader() {
        System.out.println("\n------------------------");
        System.out.println("# " + this.getTitle() + "\n");
    }

    protected abstract void printContent();

    private void print() {
        this.printHeader();
        this.printContent();
    }

    protected abstract void logic();

    protected void loop() {
        this.setStayInView(true);

        while (this.stayInView) {
            this.setProcessInput(true);
            this.print();

            while (this.processInput) {
                this.logic();
            }
        }
    }
}