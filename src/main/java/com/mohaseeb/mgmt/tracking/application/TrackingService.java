package com.mohaseeb.mgmt.tracking.application;

import com.mohaseeb.mgmt.tracking.domain.Segment;
import org.joda.time.Instant;


import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface TrackingService {
    List<Segment> getAll();

    default Segment start(Instant timeStamp, boolean absent, String note) {
        // Last segment, if any, should be closed
        Segment last = getLast();
        if (last != null && last.isOpen())
            throw new IllegalStateException("Last segment is not closed: " + last);

        // Store a new segment
        Segment segment = new Segment();
        segment.setStart(timeStamp);
        segment.setAbsent(absent ? 1 : 0);
        if (note != null) segment.setNotes(note);
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
        last.setNotes(note != null ? last.getNotes() + " | " + note : last.getNotes());
        return replaceLast(last);
    }

    default List<Serializable> summaryBetween(Instant start, Instant end) {
        return summaryBetween(start, end, millis -> millis / (1000. * 60. * 60.));
    }

    default List<Serializable> summaryBetween(Instant start, Instant end, FromLong millisToHours) {
        List<Segment> segments = getBetween(start, end);
        double workingHours = sumSegments(segments, s -> s.getAbsent() == 0, millisToHours);
        double absentHours = sumSegments(segments, s -> s.getAbsent() == 1, millisToHours);
        String notes = segments
                .stream()
                .map(Segment::getNotes)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));
        return Arrays.asList(workingHours, absentHours, notes);
    }

    default double sumSegments(List<Segment> segments, Function<Segment, Boolean> filter, FromLong millisToHours) {
        return segments
                .stream()
                .filter(filter::apply)
                .map(Segment::getDuration)
                .map(millisToHours::convert)
                .mapToDouble(d -> d)
                .sum();
    }

    List<Segment> getBetween(Instant start, Instant end);

    Segment getLast();

    Segment append(Segment segment);

    Segment replaceLast(Segment segment);

    Segment delete(int segmentId);
}

interface FromLong {
    double convert(long value);
}