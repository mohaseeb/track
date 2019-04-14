package com.mohaseeb.mgmt.tracking.domain;

import lombok.*;
import org.joda.time.Instant;

import javax.persistence.*;
//import java.time.Instant;


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

//    public static void main(String[] args) throws ParseException {
//
//        Instant instant = Instant.parse("2019-04-14T04:50:20.142Z");
//
//        LocalDateTime now = new LocalDateTime();
//        LocalDateTime monday = now.withDayOfWeek(DateTimeConstants.MONDAY);
//        LocalDateTime tomorrow = now.plusDays(1);
//        LocalDateTime weekLater = now.plusDays(7);
//        System.out.println(monday);
//        System.out.println(now);
//        System.out.println("joda -> instant " + Instant.ofEpochMilli(now.toDateTime().toInstant().getMillis()));
////        System.out.println("instnt -> joda" + joda.time.Instant());
//        System.out.println(tomorrow);
//        System.out.println(weekLater);
//
//    }
}
