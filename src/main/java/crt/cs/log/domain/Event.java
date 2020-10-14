package crt.cs.log.domain;

import lombok.Data;

@Data
public class Event {
    private String id;
    private Long duration;
    private String type;
    private String host;
    private boolean alert;

    public Event(final LogEntry logEntry) {
        this.id = logEntry.getId();
        this.type = logEntry.getType();
        this.host = logEntry.getHost();
    }
}