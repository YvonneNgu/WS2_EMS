package com.example.workshop2.cert;

import com.example.workshop2.model.Event;

public interface EventCallback {
    void onEventLoaded(Event event);
}
