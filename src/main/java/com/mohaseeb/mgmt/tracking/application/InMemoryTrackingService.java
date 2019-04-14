package com.mohaseeb.mgmt.tracking.application;

import com.mohaseeb.mgmt.tracking.domain.Segment;

import java.util.ArrayList;
import java.util.List;

public class InMemoryTrackingService implements TrackingService {
    private List<Segment> segments;

    InMemoryTrackingService() {
        segments = new ArrayList<>();
    }

    @Override
    public List<Segment> getAll() {
        return segments;
    }

    public Segment getLast() {
        Segment last = null;
        if (!segments.isEmpty())
            last = segments.get(segments.size() - 1);
        return last;
    }

    public Segment append(Segment segment){
        segments.add(segment);
        return segment;
    }

    public Segment replaceLast(Segment segment){
        segments.remove(segments.size() - 1);
        segments.add(segment);
        return segment;
    }
}
