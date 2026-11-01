package mlops.observer;

import mlops.event.Event;

@FunctionalInterface
public interface Observer<E extends Event> {

    void onEvent(E event);
}
