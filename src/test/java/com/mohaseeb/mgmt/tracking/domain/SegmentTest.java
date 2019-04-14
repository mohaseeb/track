package com.mohaseeb.mgmt.tracking.domain;

import org.joda.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



class SegmentTest {
    Segment segmentUT;

    @BeforeEach
    void setup(){
        segmentUT = new Segment();
        segmentUT.setId(1);
        segmentUT.setStart(Instant.now());
        segmentUT.setEnd(Instant.now());
    }

    @Test
    void testToString(){
        System.out.println(segmentUT.toString());
    }

}