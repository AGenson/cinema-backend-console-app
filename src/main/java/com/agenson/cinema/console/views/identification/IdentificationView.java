package com.agenson.cinema.console.views.identification;

import com.agenson.cinema.console.template.AbstractStatelessView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IdentificationView extends AbstractStatelessView {

    private final SignUpView signUpView;

    private final LogInView logInView;

    @Override
    protected String getTitle() {
        return "Identification";
    }

    @Override
    protected void printContent() {
        System.out.println("Please select an action:");
        System.out.println("[0] - Go back");
        System.out.println("[1] - Sign up");
        System.out.println("[2] - Log in\n");
    }

    @Override
    protected void logic() {
        this.setStayInView(false);
        this.setProcessInput(false);

        String input = this.getInput();

        switch (input) {
            case "0":
                break;

            case "1":
                signUpView.handler();
                break;

            case "2":
                logInView.handler();
                break;

            default:
                this.informError();
                this.setProcessInput(true);
                break;
        }
    }
}
