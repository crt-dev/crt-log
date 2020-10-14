package crt.cs.log.service;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * ScannerFactory
 * Creates a scanner for the give file
 */
@Slf4j
public class ScannerFactory {

    public Scanner create(final String fileName) throws FileNotFoundException {
        log.info("Reading log text from {}", fileName);
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        FileInputStream inputStream = new FileInputStream(file.getPath());
        return new Scanner(inputStream, "UTF-8");
    }
}