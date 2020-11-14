package com.agenson.cinema.ticket.seat;

import com.agenson.cinema.utils.CallableOneArgument;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class SeatUnitTests {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    @Test
    public void letterFromNumber_ShouldReturnLetter_WhenGivenAssociatedNumber() {
        for (int i = 0; i < ALPHABET.length(); i++) {
            String result = Seat.letterFromNumber(i+1);

            assertThat(result).isNotNull();
            assertThat(result.charAt(0)).isEqualTo(ALPHABET.charAt(i));
        }
    }

    @Test
    public void letterFromNumber_ShouldReturnNull_WhenGivenInvalidNumbers() {
        for (int i = 0; i > -ALPHABET.length(); i--)
            assertThat(Seat.letterFromNumber(i)).isNull();

        for (int i = ALPHABET.length()+1; i < 2*ALPHABET.length(); i++)
            assertThat(Seat.letterFromNumber(i)).isNull();
    }

    @Test
    public void numberFromLetter_ShouldReturnAssociatedNumber_WhenGivenLetter() {
        for (int i = 0; i < ALPHABET.length(); i++)
            assertThat(Seat.numberFromLetter(ALPHABET.charAt(i))).isEqualTo(i+1);
    }

    @Test
    public void numberFromLetter_ShouldReturnZero_WhenGivenInvalidLetters() {
        this.testRowOutOfBound(character -> assertThat(Seat.numberFromLetter(character)).isZero());
    }

    @Test
    public void fromString_ShouldReturnNewSeat_WhenGivenFormattedSeat() {
        for (char c : ALPHABET.toCharArray()) {
            for (int i = 1; i < Seat.MAX_COL; i++) {
                String formatted = String.format("%c%02d", c, i);

                assertThat(Seat.fromString(formatted).toString()).isEqualTo(formatted);
            }
        }
    }

    @Test
    public void fromString_ShouldThrowAssociatedInvalidSeatException_WhenGivenInvalidFormattedSeat() {
        for (String str : Arrays.asList(null, "", "B", "A#"))
            assertThatExceptionOfType(InvalidSeatException.class)
                    .isThrownBy(() -> Seat.fromString(str))
                    .withMessage(InvalidSeatException.Type.FORMAT.toString());

        assertThatExceptionOfType(InvalidSeatException.class)
                .isThrownBy(() -> Seat.fromString("A##"))
                .withMessage(InvalidSeatException.Type.FORMAT.toString());

        this.testRowOutOfBound(character -> {
            assertThatExceptionOfType(InvalidSeatException.class)
                    .isThrownBy(() -> Seat.fromString(character + "01"))
                    .withMessage(InvalidSeatException.Type.ROW.toString());
        });

        this.testColumnOutOfBound(value -> {
            assertThatExceptionOfType(InvalidSeatException.class)
                    .isThrownBy(() -> Seat.fromString(String.format("A%02d", value)))
                    .withMessage(InvalidSeatException.Type.COL.toString());
        });
    }

    private void testRowOutOfBound(CallableOneArgument<Character> callable) {
        for (char c = 0; c < 'A'; c++)
            callable.call(c);

        for (char c = 255; c > 'Z'; c--)
            callable.call(c);
    }

    private void testColumnOutOfBound(CallableOneArgument<Integer> callable) {
        for (int i = 0; i > -Seat.MAX_COL; i--)
            callable.call(i);

        for (int i = Seat.MAX_COL+1; i < 2*Seat.MAX_COL; i++)
            callable.call(i);
    }
}
