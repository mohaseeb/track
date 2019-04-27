package com.mohaseeb.mgmt.tracking.application;

import com.mohaseeb.mgmt.tracking.domain.Segment;
import org.joda.time.Instant;


import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public interface TrackingService {
    List<Segment> getAll();

    default Segment start(Instant timeStamp, String note) {
        // Last segment, if any, should be closed
        Segment last = getLast();
        if (last != null && last.isOpen())
            throw new IllegalStateException("Last segment is not closed: " + last);

        // Store a new segment
        Segment segment = new Segment();
        segment.setStart(timeStamp);
        segment.setNotes(note);
        return append(segment);
    }

    default Segment end(Instant timeStamp, String note) {
        // Last segment, should be open
        Segment last = getLast();
        if (last == null || !last.isOpen())
            throw new IllegalStateException("Last segment missing or closed: " + last);

        // update last segment
        last.setEnd(timeStamp);
        last.setDuration(last.getEnd().getMillis() - last.getStart().getMillis());
        last.setNotes(note.length() > 0 ? last.getNotes() + " | " + note : last.getNotes());
        return replaceLast(last);
    }

    default List<Serializable> summaryBetween(Instant start, Instant end) {
        return summaryBetween(start, end, millis -> millis / (1000. * 60. * 60.));
    }

    default List<Serializable> summaryBetween(Instant start, Instant end, FromSecondsConverter converter) {
        List<Segment> segments = getBetween(start, end);
        long millis = segments
                .stream()
                .mapToLong(Segment::getDuration)
                .sum();
        String notes = segments
                .stream()
                .map(Segment::getNotes)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));
        return Arrays.asList(converter.convert(millis), notes);
    }

    List<Segment> getBetween(Instant start, Instant end);

    Segment getLast();

    Segment append(Segment segment);

    Segment replaceLast(Segment segment);
}

interface FromSecondsConverter {
    double convert(long seconds);
}