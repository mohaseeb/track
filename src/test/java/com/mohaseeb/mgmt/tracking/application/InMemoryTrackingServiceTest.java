package com.mohaseeb.mgmt.tracking.application;

import com.mohaseeb.mgmt.tracking.domain.Segment;
import org.joda.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTrackingServiceTest {
    private TrackingService service;

    @BeforeEach
    void init() {
        service = new InMemoryTrackingService();
    }

    @Test
    void getAll() {
        assertEquals(service.getAll().size(), 0);
    }

    @Test
    void testStart() {
        Instant ts = Instant.now();
        Segment segment = service.start(ts, false, "");

        assertEquals(0, segment.getId());
        assertEquals(ts, segment.getStart());
        assertNull(segment.getEnd());
        assertEquals(0, segment.getDuration());
    }

    @Test
    void testStartOpenSegment() {
        Instant ts = Instant.now();
        Segment segment = service.start(ts, false, "");
        assertEquals(0, segment.getId());

        // Add a second segment before closing the first one
        assertThrows(IllegalStateException.class, () -> service.start(Instant.now(), false, ""));
    }


    @Test
    void end() {
        Instant ts = Instant.now();
        service.start(ts, false, "");

        int elapsedMillis = 10 * 1000;
        Instant ts2 = Instant.ofEpochMilli(ts.getMillis() + elapsedMillis);
        Segment closedSegment = service.end(ts2, "");

        assertEquals(elapsedMillis, closedSegment.getDuration());

        // Check it is OK to open a new segment
        Segment newSegment = service.start(Instant.now(), false, "");
        assertNotNull(newSegment.getStart());
        assertNull(newSegment.getEnd());
        assertEquals(2, service.getAll().size());
    }

    @Test
    void endZeroSegments() {
        assertThrows(IllegalStateException.class, () -> service.end(Instant.now(), ""));
    }

    @Test
    void endClosedSegment() {
        service.start(Instant.now(), false, "");
        Segment closed = service.end(Instant.now(), "");
        assertEquals(1, service.getAll().size());
        assertNotNull(closed.getEnd());

        assertThrows(IllegalStateException.class, () -> service.end(Instant.now(), ""));
    }
}