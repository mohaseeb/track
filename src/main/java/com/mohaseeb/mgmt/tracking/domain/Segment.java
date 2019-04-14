package com.mohaseeb.mgmt.tracking.domain;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Segment {
    @Id
    @GeneratedValue
    private int id;
    @Column
    private Instant start;
    @Column
    private Instant end;
    @Column
    private long duration;

    public boolean isOpen() {
        return end == null;
    }
}
