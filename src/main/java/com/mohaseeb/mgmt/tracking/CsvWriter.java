package com.mohaseeb.mgmt.tracking;

import com.mohaseeb.mgmt.tracking.domain.Segment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class CsvWriter {
    public static void write(List<Segment> segments, String filePath) throws IOException {
        writeToFile(content(segments), filePath);
    }

    private static String content(List<Segment> segments) {
        String header = Segment.header();
        String rows = segments.stream().map(Segment::values).collect(Collectors.joining("\n"));
        return header + "\n" + rows;
    }

    private static void writeToFile(String content, String filePath) throws IOException {
        Files.write(Paths.get(filePath), content.getBytes());
    }
}