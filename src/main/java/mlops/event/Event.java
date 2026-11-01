package mlops.event;

import java.time.Instant;

public interface Event {

    EventType type();
    Instant timestamp();
}
