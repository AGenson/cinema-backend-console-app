package com.agenson.cinema.console.views;

import com.agenson.cinema.console.template.AbstractStatelessView;
import com.agenson.cinema.console.views.identification.IdentificationView;
import com.agenson.cinema.console.views.management.StaffMenuView;
import com.agenson.cinema.security.SecurityService;
import com.agenson.cinema.security.SecurityRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MainMenuView extends AbstractStatelessView {

    private final SecurityService securityService;

    private final IdentificationView identificationView;

    private final StaffMenuView staffMenuView;

    @Override
    protected String getTitle() {
        return "Main Menu";
    }

    @Override
    protected void printContent() {
        Optional<String> username = this.securityService.getCurrentUser().map(SecurityService.UserDetails::getUsername);

        username.ifPresent(str -> System.out.println("Logged in as: " + str + "\n"));

        System.out.println("Please select an action:");

        if (this.securityService.hasRole(SecurityRole.STAFF))
            System.out.println("[0] - Go to staff section");

        System.out.println("[1] - See movie catalog");

        if (this.securityService.isLoggedIn())
            System.out.println("[2] - Log out");
        else
            System.out.println("[2] - Identify");

        System.out.println("[3] - Leave\n");
    }

    @Override
    protected void logic() {
        this.setProcessInput(false);

        String input = this.getInput();

        switch (input) {

            case "1":
                System.out.println("Going to movie catalog...");
                break;

            case "2":
                if (this.securityService.isLoggedIn())
                    this.securityService.logout();
                else
                    this.identificationView.handler();
                break;

            case "3":
                this.setStayInView(false);;
                break;

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
