package com.agenson.cinema.console.views.identification;

import com.agenson.cinema.console.template.AbstractStatelessView;
import com.agenson.cinema.security.SecurityException;
import com.agenson.cinema.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LogInView extends AbstractStatelessView {

    private final SecurityService securityService;

    @Override
    protected String getTitle() {
        return "Log In";
    }

    @Override
    protected void printContent() {
        // IGNORED
    }

    @Override
    public void logic() {
        this.setStayInView(false);
        this.setProcessInput(false);

        String username = this.getInput("Username");
        String password = this.getInput("Password");

        try {
            this.securityService.login(username, password);
        } catch (SecurityException ex) {
            System.out.println("\n" + ex.getMessage());
            this.askRetry();
        }
    }
}
