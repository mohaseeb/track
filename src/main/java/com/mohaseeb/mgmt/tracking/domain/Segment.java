package com.mohaseeb.mgmt.tracking.domain;

import lombok.*;

import java.time.Instant;

@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Segment {
    private int id;
    private Instant start;
    private Instant end;
    private long duration;

    public boolean isOpen() {
        return end == null;
    }
}
