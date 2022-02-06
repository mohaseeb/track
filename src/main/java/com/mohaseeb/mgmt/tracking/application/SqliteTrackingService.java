package com.mohaseeb.mgmt.tracking.application;

import com.mohaseeb.mgmt.tracking.domain.Segment;
import com.mohaseeb.mgmt.tracking.sqlite.SegmentRepository;
import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.List;

public class SqliteTrackingService implements TrackingService {

    @Autowired
    private SegmentRepository repository;

    @Override
    public List<Segment> getAll() {
        return repository.findAll();
    }

    @Override
    public List<Segment> getBetween(Instant start, Instant end) {
        return repository.findAllByStartBetween(start, end);
    }

    @Override
    public Segment getLast() {
        return repository.findFirstByOrderByIdDesc();
    }

    @Override
    public Segment append(Segment segment) {
        return repository.save(segment);
    }

    @Override
    public Segment replaceLast(Segment segment) {
        return repository.save(segment);
    }

    @Override
    public Segment delete(int segmentId) {
        Segment segment = this.findById(segmentId);
        this.delete(segment);
        return segment;
    }

    public void delete(Segment segment) {
        this.repository.delete(segment);
    }

    public Segment findById(int segmentId) {
        return this.getAll().stream().filter(s -> s.getId() == segmentId).findFirst().orElseThrow();
    }

}
