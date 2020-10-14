package crt.cs.log.service;

import crt.cs.log.domain.Event;
import crt.cs.log.domain.LogEntry;

/**
 * EventFactory
 * Creates an event based on matched log entries
 */
public class EventFactory {

    private static final int ALERT_THRESHOLD = 4;

    public Event create(final LogEntry startEntry, final LogEntry finishEntry) {
        Event event = new Event(startEntry);
        long duration = finishEntry.getTimestamp() - startEntry.getTimestamp();
        event.setDuration(duration);
        event.setAlert(duration >= ALERT_THRESHOLD);
        return event;
    }
}
