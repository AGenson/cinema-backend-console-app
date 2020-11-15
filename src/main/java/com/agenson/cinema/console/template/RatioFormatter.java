package com.agenson.cinema.console.template;

public interface RatioFormatter {

    static String format(int value, int max) {
        double result = (value * 1.0) / max * 100;

        return String.format("%d/%d (%.02f%%)", value, max, result);
    }
}
