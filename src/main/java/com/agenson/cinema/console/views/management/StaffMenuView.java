package com.agenson.cinema.console.views.management;

import com.agenson.cinema.console.template.AbstractStatelessView;
import com.agenson.cinema.console.views.management.movies.ManageMoviesView;
import com.agenson.cinema.console.views.management.users.ManageUsersView;
import com.agenson.cinema.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StaffMenuView extends AbstractStatelessView {

    private final SecurityService securityService;

    private final ManageMoviesView manageMoviesView;

    private final ManageUsersView manageUsersView;

    @Override
    protected String getTitle() {
        return "Staff Menu";
    }

    @Override
    protected void printContent() {
        this.securityService.getCurrentUser().ifPresent(user -> System.out.println("Logged in as: " + user + "\n"));

        System.out.println("Please select an action:");
        System.out.println("[0] - Go to customer section");
        System.out.println("[1] - Manage Movies");
        System.out.println("[2] - Manage Rooms");
        System.out.println("[3] - Manage Users\n");
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
                this.manageMoviesView.handler();
                break;

            case "2":
                System.out.println("Going to manage rooms...");
                break;

            case "3":
                this.manageUsersView.handler();
                break;

            default:
                this.informError();
                this.setProcessInput(true);
                break;
        }
    }
}
