package crt.cs.log;

import crt.cs.log.service.DatabaseManager;
import crt.cs.log.service.LineProducer;
import crt.cs.log.service.ScannerFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Scanner;

@Slf4j
public class Application {
    public static void main(final String[] args) {

        if (args.length == 0 || args[0] == null || args[0].isEmpty()) {
            log.error("expected filename parameter");
            return;
        }

        ScannerFactory scannerFactory = new ScannerFactory();
        Scanner scanner = null;
        try {
            scanner = scannerFactory.create(args[0]);
        } catch (FileNotFoundException ex) {
            log.error("File [{}] does not exist: ", ex);
            return;
        }

        DatabaseManager databaseManager = new DatabaseManager();
        try {
            databaseManager.create();
        } catch (SQLException | ClassNotFoundException ex) {
            log.error("Database could not be initialised due to: ", ex);
            return;
        }

        LineProducer lineProducer = new LineProducer();
        lineProducer.read(scanner);
        databaseManager.countEvents();
        log.info("Log Analysis complete");
    }
}
