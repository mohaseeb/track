package com.mohaseeb.mgmt.tracking.sqlite;


import com.mohaseeb.mgmt.tracking.domain.Segment;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.List;

@Transactional
public interface SegmentRepository extends JpaRepository<Segment, String> {
    Segment findFirstByOrderByIdDesc();

    List<Segment> findAllByStartBetween(Instant start, Instant end);
}
