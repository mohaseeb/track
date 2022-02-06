package com.mohaseeb.mgmt.tracking.domain;

import com.mohaseeb.mgmt.tracking.TimeUtils;
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
    @Column(name="absent", columnDefinition="INTEGER default '0'")
    private int absent;

    public boolean isOpen() {
        return end == null;
    }


    public static String header() {
        return commaJoin("id", "start", "end", "duration", "notes");
    }

    public String values() {
        return commaJoin(
                String.valueOf(id),
                TimeUtils.localDateTimeFormat(start),
                TimeUtils.localDateTimeFormat(end),
                String.valueOf(duration / (1000. * 60. * 60.)),
                String.join("|", notes)

        );
    }

    private static String commaJoin(String... entries) {
        return String.join(",", entries);
    }
}
