package com.agenson.cinema.ticket.seat;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Seat {

    public static final int MAX_ROW = 26;
    public static final int MAX_COL = 52;

    private final int row;
    private final int col;

    private Seat(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public String getRowString() {
        return Seat.letterFromNumber(this.row);
    }

    public String getColString() {
        return String.format("%02d", this.col);
    }

    public static Seat fromString(String seat) throws InvalidSeatException {
        if (seat == null || seat.length() == 0) throw new InvalidSeatException(InvalidSeatException.Type.FORMAT);

        int row = Seat.numberFromLetter(seat.charAt(0));
        int col;

        try {
            col = Integer.parseInt(seat.substring(1));
        } catch (NumberFormatException ignored) {
            throw new InvalidSeatException(InvalidSeatException.Type.FORMAT);
        }

        if (row < 1) throw new InvalidSeatException(InvalidSeatException.Type.ROW);
        else if (col < 1) throw new InvalidSeatException(InvalidSeatException.Type.COL);
        else if (col > MAX_COL) throw new InvalidSeatException(InvalidSeatException.Type.COL);

        return new Seat(row, col);
    }

    public static String letterFromNumber(int number) {
        return number >= 1 && number <= 26 ? String.valueOf((char)(number + 'A' - 1)) : null;
    }

    public static int numberFromLetter(char letter) {
        return letter >= 'A' && letter <= 'Z' ? (letter - 'A' + 1) : 0;
    }

    @Override
    public String toString() {
        return String.format("%s%s", this.getRowString(), this.getColString());
    }
}
