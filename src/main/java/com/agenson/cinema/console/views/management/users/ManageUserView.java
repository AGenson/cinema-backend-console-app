package com.agenson.cinema.console.views.management.users;

import com.agenson.cinema.console.template.AbstractStateView;
import com.agenson.cinema.security.SecurityRole;
import com.agenson.cinema.user.UserDetailsDTO;
import com.agenson.cinema.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ManageUserView extends AbstractStateView<UserDetailsDTO> {

    private final UserService userService;

    @Override
    protected void refreshState() {
        // IGNORED
    }

    @Override
    protected String getTitle() {
        return "Manage User";
    }

    @Override
    protected void printContent() {
        System.out.println("User: " + this.state + "\n");

        System.out.println("Please select an action:");
        System.out.println("[0] - Go back");

        if (SecurityRole.STAFF.equals(this.state.getRole()))
            System.out.println("[1] - Remove staff privilege\n");
        else
            System.out.println("[1] - Give staff privilege\n");
    }

    @Override
    protected void logic() {
        this.setProcessInput(false);

        String input = this.getInput();

        switch (input) {

            case "1":
                this.userService.updateUserRole(
                        this.state.getUuid(),
                        SecurityRole.STAFF.equals(this.state.getRole()) ? SecurityRole.CUSTOMER : SecurityRole.STAFF
                );

            case "0":
                this.setStayInView(false);
                break;

            default:
                this.informError();
                this.setProcessInput(true);
                break;
        }
    }
}
