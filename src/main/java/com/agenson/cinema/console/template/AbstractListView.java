package com.agenson.cinema.console.template;

import java.util.Collections;
import java.util.List;

public abstract class AbstractListView<T> extends AbstractStatelessView {

    protected List<T> list = Collections.emptyList();

    protected abstract void refreshList();

    @Override
    public void handler() {
        this.refreshList();
        super.handler();
    }
}
