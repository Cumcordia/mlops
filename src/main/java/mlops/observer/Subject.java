package mlops.observer;

import java.util.Collection;

import mlops.event.Event;
import mlops.event.EventType;

public interface Subject<E extends Event> {

    void subscribe(EventType type, Observer<? super E> observer);
    void unsubscribe(EventType type, Observer<? super E> observer);

    default void subscribe(Collection<EventType> types, Observer<? super E> observer) {
        types.forEach(type -> subscribe(type, observer));
    }
}
