package mlops.observer;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import mlops.event.Event;
import mlops.event.EventType;

public abstract class AbstractSubject<E extends Event> implements Subject<E> {

    private static final System.Logger LOG = System.getLogger(AbstractSubject.class.getName());

    private final Map<EventType, CopyOnWriteArrayList<Observer<? super E>>> observers = new ConcurrentHashMap<>();

    @Override
    public void subscribe(EventType type, Observer<? super E> observer) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(observer, "observer");
        observers.computeIfAbsent(type, key -> new CopyOnWriteArrayList<>()).addIfAbsent(observer);
    }

    @Override
    public void unsubscribe(EventType type, Observer<? super E> observer) {
        CopyOnWriteArrayList<Observer<? super E>> subscribed = observers.get(type);
        if (subscribed != null) {
            subscribed.remove(observer);
        }
    }

    protected final void publish(E event) {
        CopyOnWriteArrayList<Observer<? super E>> subscribed = observers.get(event.type());
        if (subscribed == null) {
            return;
        }
        for (Observer<? super E> observer : subscribed) {
            notifyObserver(observer, event);
        }
    }

    private void notifyObserver(Observer<? super E> observer, E event) {
        try {
            observer.onEvent(event);
        } catch (RuntimeException e) {
            LOG.log(System.Logger.Level.ERROR, "Observer failed on " + event.type() + ", continuing", e);
        }
    }
}
