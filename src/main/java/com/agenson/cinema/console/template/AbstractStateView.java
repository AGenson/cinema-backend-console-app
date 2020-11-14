package com.agenson.cinema.console.template;

public abstract class AbstractStateView<T> extends AbstractView {

    protected T state;

    protected abstract void refreshState();

    public void handler(T state) {
        this.state = state;
        this.loop();
    }
}
