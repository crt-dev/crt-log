package crt.cs.log.service;

import crt.cs.log.domain.Event;
import crt.cs.log.domain.LogEntry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * EntryMatchingService
 * Pushes events to database created from matching log entries
 */
@Slf4j
@RequiredArgsConstructor
public class EntryMatchingService implements Runnable {

    @NonNull
    private Map<String, LogEntry> startUnmatched;

    @NonNull
    private Map<String, LogEntry> finishUnmatched;

    @Override
    public void run() {
        if (this.startUnmatched.size() != 0 && this.finishUnmatched.size() != 0) {
            long startTime = System.currentTimeMillis();
            List<Event> events = this.createEvents();

            DatabaseManager databaseManager = new DatabaseManager();
            try {
                databaseManager.init();
                databaseManager.insert(events);
            } catch (SQLException | ClassNotFoundException ex) {
                log.error("failed to insert events in database due to: {}", ex);
            }

            long endTime = System.currentTimeMillis();
            log.debug("Matched {} unmatched entries in {}ms [remaining unmatched start:{} finish:{}] ",
                    events.size(), endTime - startTime, startUnmatched.size(), finishUnmatched.size());
        }
    }

    private List<Event> createEvents() {
        List<Event> events = new ArrayList<>();
        EventFactory eventFactory = new EventFactory();
        Iterator<Map.Entry<String, LogEntry>> i = startUnmatched.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<String,LogEntry> startEntry = i.next();
            LogEntry finishedEntry = finishUnmatched.get(startEntry.getKey());

            if (finishedEntry != null) {
                events.add(eventFactory.create(startEntry.getValue(), finishedEntry));
                this.finishUnmatched.remove(startEntry.getKey());
                i.remove();
            }
        }
        return events;
    }
}