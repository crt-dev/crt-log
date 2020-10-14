package crt.cs.log.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import crt.cs.log.domain.Event;
import crt.cs.log.domain.LogEntry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LogAnalysisService
 */
@Slf4j
@RequiredArgsConstructor
public class LogAnalysisService {

    private static final String STARTED = "STARTED";

    private static final String FINISHED = "FINISHED";

    @NonNull
    private ObjectMapper objectMapper;

    @NonNull
    private Map<String, LogEntry> startUnmatched;

    @NonNull
    private Map<String, LogEntry> finishedUnmatched;


    public List<Event> createEvents(final List<String> lines) {
        Map<String, LogEntry> startedMap = new HashMap<>();
        Map<String, LogEntry> finishedMap = new HashMap<>();
        EventFactory eventFactory = new EventFactory();

        List<Event> events = new ArrayList<>();
        for (String line : lines) {
            LogEntry entry = this.readEntry(line);

            if (entry.getState().equals(STARTED)) {
                LogEntry finishedEntry = finishedMap.get(entry.getId());
                if (finishedEntry == null) {
                    startedMap.put(entry.getId(), entry);
                } else {
                    events.add(eventFactory.create(entry, finishedEntry));
                    finishedMap.remove(entry.getId());
                }
            } else if (entry.getState().equals(FINISHED)) {
                LogEntry startEntry = startedMap.get(entry.getId());
                if (startEntry == null) {
                    finishedMap.put(entry.getId(), entry);
                } else {
                    events.add(eventFactory.create(startEntry, entry));
                    startedMap.remove(entry.getId());
                }
            }
        }

        log.info("Matched {} events, {} unmatched start logs, {} unmatched finished logs",
                events.size(), startedMap.size(), finishedMap.size());

        if (!startedMap.isEmpty()) {
            startUnmatched.putAll(startedMap);
        }

        if (!finishedMap.isEmpty()) {
            finishedUnmatched.putAll(finishedMap);
        }

        return events;
    }

    private LogEntry readEntry(final String line) {
        LogEntry entry = null;
        try {
            entry = this.objectMapper.readValue(line, LogEntry.class);
        } catch (JsonProcessingException ex) {
            log.error("Cannot read line [[]] due to: ", line, ex);
        }
        return entry;
    }
}