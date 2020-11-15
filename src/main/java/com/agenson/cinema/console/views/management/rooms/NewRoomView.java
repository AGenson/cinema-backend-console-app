package com.agenson.cinema.console.views.management.rooms;

import com.agenson.cinema.console.template.AbstractStatelessView;
import com.agenson.cinema.room.InvalidRoomException;
import com.agenson.cinema.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class NewRoomView extends AbstractStatelessView {

    private final RoomService roomService;

    @Override
    protected String getTitle() {
        return "New Room";
    }

    @Override
    protected void printContent() {
        this.informCancel();
    }

    @Override
    protected void logic() {
        this.setStayInView(false);

        ArrayList<Integer> inputs = new ArrayList<>();

        for (String field : Arrays.asList("Room number", "Number of rows", "Number of seats per row")) {
            boolean sameInput = true;

            while (sameInput) {
                String numberStr = this.getInput(field);

                if ("c".equals(numberStr.toLowerCase())) {
                    this.setProcessInput(false);
                    return;
                } else if (numberStr.matches("^[+-]?\\d+$")) {
                    inputs.add(Integer.parseInt(numberStr));
                    sameInput = false;
                } else {
                    this.informError();
                }
            }
        }

        try {
            this.roomService.createRoom(inputs.get(0), inputs.get(1), inputs.get(2));
            this.setProcessInput(false);
        } catch (InvalidRoomException ex) {
            System.out.println("\n" + ex.getMessage());
            this.setProcessInput(true);
        }
    }
}
