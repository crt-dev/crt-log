package crt.cs.log.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import crt.cs.log.domain.LogEntry;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class LineProducer {

    private static final int DISPATCH_SIZE = 10000;
    private static final int UNMATCHED_LIMIT = 25;
    private static final int THREAD_POOL_SIZE = 4;

    public List<String> read(final Scanner scanner) {
        ObjectMapper objectMapper = new ObjectMapper();
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        Map<String, LogEntry> startUnmatched = new ConcurrentHashMap<>();
        Map<String, LogEntry> finishedUnmatched = new ConcurrentHashMap<>();

        List<String> lines = new ArrayList<>();
        int counter = 0;
        long startTime = System.currentTimeMillis();
        while (scanner.hasNextLine()) {
            lines.add(scanner.nextLine());
            if (lines.size() % DISPATCH_SIZE == 0) {
                log.debug("Creating dispatch of size {} @ line {}", lines.size(), counter);
                List<String> deepCopy = lines.stream().map(String::new).collect(Collectors.toList());
                lines.clear();
                LineConsumer runner =
                        new LineConsumer(deepCopy, objectMapper, startUnmatched, finishedUnmatched);
                executorService.execute(runner);
            }
            counter++;
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            log.error("Could not terminate executorService due to: {}", executorService);
        }

        if (!lines.isEmpty()) {
            LineConsumer lineConsumer = new LineConsumer(lines, objectMapper, startUnmatched, finishedUnmatched);
            lineConsumer.run();
        }

        log.debug("finished with unmatched items: [start:{} finished:{}]",
                startUnmatched.size(), finishedUnmatched.size());

        EntryMatchingService matchingService = new EntryMatchingService(startUnmatched, finishedUnmatched);
        matchingService.run();

        long endTime = System.currentTimeMillis();
        log.info("Finished scanning file with {} lines in {}ms",  counter, endTime - startTime);

        if (startUnmatched.size() + finishedUnmatched.size() != 0) {
            log.error("There are unmatched log entries");
        }

        return lines;
    }


}