package testsupport;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import mlops.event.EventType;
import mlops.event.MetricEvent;

public final class TestEvents {

    public static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");
    public static final Clock CLOCK = Clock.fixed(T0, ZoneOffset.UTC);

    private TestEvents() {
    }

    public static MetricEvent metric(EventType type, String sourceId, String stageId, double value) {
        return metricAt(0, type, sourceId, stageId, value);
    }

    public static MetricEvent metricAt(long seconds, EventType type, String sourceId, String stageId, double value) {
        return new MetricEvent(type, T0.plusSeconds(seconds), sourceId, stageId, value);
    }
}
