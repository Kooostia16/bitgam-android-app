package com.bitgam.app.ui;

public interface UiInformableCallback<T> extends UiCallback<T> {
    void inform(String text);
}
