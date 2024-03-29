package com.mohaseeb.mgmt.tracking.application;

import com.mohaseeb.mgmt.tracking.domain.Segment;

import org.hibernate.cfg.NotYetImplementedException;
import org.joda.time.Instant;

import java.util.ArrayList;
import java.util.List;

public class InMemoryTrackingService implements TrackingService {
    private List<Segment> segments;
    private int idTracker;

    InMemoryTrackingService() {
        segments = new ArrayList<>();
        idTracker = 0;
    }

    @Override
    public List<Segment> getAll() {
        return segments;
    }

    @Override
    public List<Segment> getBetween(Instant start, Instant end) {
        return null;
    }

    public Segment getLast() {
        Segment last = null;
        if (!segments.isEmpty())
            last = segments.get(segments.size() - 1);
        return last;
    }

    public Segment append(Segment segment) {
        segment.setId(idTracker);
        idTracker++;
        segments.add(segment);
        return segment;
    }

    public Segment replaceLast(Segment segment) {
        segments.remove(segments.size() - 1);
        idTracker--;
        return append(segment);
    }

    @Override
    public Segment delete(int segmentId) {
        throw new NotYetImplementedException("yep");
    }
}
