package com.mohaseeb.mgmt.tracking.sqlite;


import com.mohaseeb.mgmt.tracking.domain.Segment;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;

@Transactional
public interface SegmentRepository extends JpaRepository<Segment, String> {
    Segment findFirstByOrderByIdDesc();
}
