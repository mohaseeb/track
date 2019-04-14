package com.mohaseeb.mgmt.tracking.application;

import com.mohaseeb.mgmt.tracking.domain.Segment;

import java.time.Instant;
import java.util.List;

public interface TrackingService {
    List<Segment> getAll();

    default Segment start(Instant timeStamp) {
        // Last segment, if any, should be closed
        Segment last = getLast();
        if (last != null && last.isOpen())
            throw new IllegalStateException("Last segment is not closed: " + last);

        // Store a new segment
        Segment segment = new Segment();
        segment.setStart(timeStamp);
        return append(segment);
    }

    default Segment end(Instant timeStamp) {
        // Last segment, should be open
        Segment last = getLast();
        if (last == null || !last.isOpen())
            throw new IllegalStateException("Last segment missing or closed: " + last);

        // update last segment
        last.setEnd(timeStamp);
        last.setDuration(last.getEnd().getEpochSecond() - last.getStart().getEpochSecond());
        return replaceLast(last);
    }

    default double hoursBetween(Instant start, Instant end){
        return durationBetween(start, end, seconds -> seconds / (60. * 60.));
    }

    default double durationBetween(Instant start, Instant end, FromSecondsConverter converter){
        long seconds = getBetween(start, end).stream().mapToLong(Segment::getDuration).sum();
        return converter.convert(seconds);
    }

    List<Segment> getBetween(Instant start, Instant end);

    Segment getLast();

    Segment append(Segment segment);

    Segment replaceLast(Segment segment);
}

interface FromSecondsConverter {
    double convert(long seconds);
}