package com.agenson.cinema.console.views.management.users;

import com.agenson.cinema.console.template.AbstractListView;
import com.agenson.cinema.security.SecurityService;
import com.agenson.cinema.user.UserDetailsDTO;
import com.agenson.cinema.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ManageUsersView extends AbstractListView<UserDetailsDTO> {

    private static final int OFFSET = 1;

    private final UserService userService;

    private final SecurityService securityService;

    private final ManageUserView manageUserView;

    @Override
    protected void refreshList() {
        this.securityService.getCurrentUser().ifPresent(currentUser -> {
            this.list = this.userService.findUsers();
            this.list.removeIf(user -> currentUser.getUuid().equals(user.getUuid()));
        });
    }

    @Override
    protected String getTitle() {
        return "Manage Users";
    }

    @Override
    protected void printContent() {
        System.out.println("Please select an action or user:");
        System.out.println("[0] - Go back\n");

        for (int i = 0; i < this.list.size(); i++)
            System.out.println("[" + (i+OFFSET) + "] - " + this.list.get(i).toString());

        System.out.println();
    }

    @Override
    protected void logic() {
        this.setProcessInput(false);

        String input = this.getInput();

        try {
            int value = Integer.parseInt(input);

            if (value == 0)
                this.setStayInView(false);
            else if (value >= OFFSET && value < this.list.size() + OFFSET) {
                this.manageUserView.handler(this.list.get(value - OFFSET));
                this.refreshList();
            } else
                throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            this.informError();
            this.setProcessInput(true);
        }
    }
}
