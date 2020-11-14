package com.agenson.cinema.console.views.identification;

import com.agenson.cinema.console.template.AbstractStatelessView;
import com.agenson.cinema.security.SecurityService;
import com.agenson.cinema.user.InvalidUserException;
import com.agenson.cinema.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SignUpView extends AbstractStatelessView {

    private final UserService userService;

    private final SecurityService securityService;

    @Override
    protected String getTitle() {
        return "Sign Up";
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
        String confirmPassword = this.getInput("Confirm Password");

        try {
            if (!password.equals(confirmPassword))
                throw new InvalidUserException(InvalidUserException.Type.PASSWORD_CONFIRM);

            this.userService.createUser(username, password);
            this.securityService.login(username, password);
        } catch (InvalidUserException ex) {
            System.out.println(ex.getMessage());
            this.askRetry();
        }
    }
}
