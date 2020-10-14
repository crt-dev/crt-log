package crt.cs.log.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import crt.cs.log.domain.Event;
import crt.cs.log.domain.LogEntry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * LineConsumer
 * Consumer thread to receive invoke analysis service with list of lines and submit events to database.
 */
@Slf4j
@AllArgsConstructor
public class LineConsumer implements Runnable {

    private List<String> input;

    private ObjectMapper objectMapper;

    private Map<String, LogEntry> startUnmatched;

    private Map<String, LogEntry> finishedUnmatched;

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        LogAnalysisService service =
                new LogAnalysisService(this.objectMapper, this.startUnmatched, this.finishedUnmatched);
        List<Event> events = service.createEvents(this.input);

        DatabaseManager databaseManager = new DatabaseManager();
        try {
            databaseManager.init();
            databaseManager.insert(events);
        } catch (SQLException | ClassNotFoundException ex) {
            log.error("failed to insert events in database due to: {}", ex);
        }

        long endTime = System.currentTimeMillis();
        log.debug("Registered {} events in {}ms", events.size(), endTime - startTime);
    }
}
