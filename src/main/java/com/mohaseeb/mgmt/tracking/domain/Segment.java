package com.mohaseeb.mgmt.tracking.domain;

import lombok.*;
import org.joda.time.Instant;

import javax.persistence.*;


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
    @Column
    private String notes;

    public boolean isOpen() {
        return end == null;
    }
}
