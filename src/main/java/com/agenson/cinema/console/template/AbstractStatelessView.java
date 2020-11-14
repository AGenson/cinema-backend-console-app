package com.agenson.cinema.console.template;

public abstract class AbstractStatelessView extends AbstractView {

    public void handler() {
        this.loop();
    }
}
