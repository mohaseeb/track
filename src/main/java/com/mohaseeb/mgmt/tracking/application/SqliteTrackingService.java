package com.mohaseeb.mgmt.tracking.application;

import com.mohaseeb.mgmt.tracking.domain.Segment;
import com.mohaseeb.mgmt.tracking.sqlite.SegmentRepository;
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
}
