package com.agenson.cinema.console.views;

import com.agenson.cinema.console.template.AbstractStatelessView;
import com.agenson.cinema.console.views.customer.ProfileView;
import com.agenson.cinema.console.views.customer.catalog.MovieCatalogView;
import com.agenson.cinema.console.views.identification.IdentificationView;
import com.agenson.cinema.console.views.management.StaffMenuView;
import com.agenson.cinema.security.SecurityService;
import com.agenson.cinema.security.SecurityRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MainMenuView extends AbstractStatelessView {

    private final SecurityService securityService;

    private final IdentificationView identificationView;

    private final StaffMenuView staffMenuView;

    private final MovieCatalogView movieCatalogView;

    private final ProfileView profileView;

    @Override
    protected String getTitle() {
        return "Main Menu";
    }

    @Override
    protected void printContent() {
        this.securityService.getCurrentUser().ifPresent(user -> System.out.println("Logged in as: " + user + "\n"));

        System.out.println("Please select an action:");

        if (this.securityService.hasRole(SecurityRole.STAFF))
            System.out.println("[0] - Go to staff section");

        System.out.println("[1] - See movie catalog");

        if (this.securityService.isLoggedIn()) {
            System.out.println("[2] - See Profile");
            System.out.println("[3] - Log out");
            System.out.println("[4] - Leave\n");
        } else {
            System.out.println("[2] - Identify");
            System.out.println("[3] - Leave\n");
        }
    }

    @Override
    protected void logic() {
        this.setProcessInput(false);

        String input = this.getInput();

        switch (input) {

            case "1":
                this.movieCatalogView.handler();
                break;

            case "2":
                if (this.securityService.isLoggedIn())
                    this.profileView.handler();
                else
                    this.identificationView.handler();
                break;

            case "3":
                if (this.securityService.isLoggedIn())
                    this.securityService.logout();
                else
                    this.setStayInView(false);
                break;

            case "4":
                if (this.securityService.isLoggedIn()) {
                    this.setStayInView(false);
                    break;
                }

            case "0":
                if (this.securityService.hasRole(SecurityRole.STAFF)) {
                    this.staffMenuView.handler();
                    break;
                }

            default:
                this.informError();
                this.setProcessInput(true);
                break;
        }
    }
}
