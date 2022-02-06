package com.mohaseeb.mgmt.tracking.application;

import com.mohaseeb.mgmt.tracking.domain.Segment;
import org.joda.time.Instant;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface TrackingService {
    List<Segment> getAll();

    default Segment start(Instant timeStamp, boolean absent, String note) {
        // Last segment, if any, should be closed
        Segment last = getLast();
        if (last != null && last.isOpen())
            throw new IllegalStateException("Last segment is not closed: " + last);

        // Store a new segment
        Segment segment = new Segment();
        segment.setStart(timeStamp);
        segment.setAbsent(absent ? 1 : 0);
        if (note != null) segment.setNotes(note);
        return append(segment);
    }

    default Segment end(Instant timeStamp, String note) {
        // Last segment, should be open
        Segment last = getLast();
        if (last == null || !last.isOpen())
            throw new IllegalStateException("Last segment missing or closed: " + last);

        // update last segment
        last.setEnd(timeStamp);
        last.setDuration(last.getEnd().getMillis() - last.getStart().getMillis());
        last.setNotes(note != null ? last.getNotes() + " | " + note : last.getNotes());
        return replaceLast(last);
    }

    default Map<String, Serializable> summaryBetween(Instant start, Instant end) {
        return summaryBetween(start, end, millis -> millis / (1000. * 60. * 60.));
    }

    default Map<String, Serializable> summaryBetween(Instant start, Instant end, FromLong millisToHours) {
        List<Segment> segments = getBetween(start, end);
        double workingHours = sumSegments(segments, s -> s.getAbsent() == 0, millisToHours);
        double absentHours = sumSegments(segments, s -> s.getAbsent() == 1, millisToHours);
        String notes = segments
                .stream()
                .map(Segment::getNotes)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));

        Map<String, Serializable> summary = Map.of(
                "workingHours", workingHours,
                "absentHours", absentHours,
                "notes", notes);
        return merge(summary, getExpectedDays(start, end));
    }

    default Map<String, Serializable> merge(Map<String, Serializable> summary, Map<String, Double> expectedDays) {
        return Stream.concat(
                summary.entrySet().stream(),
                expectedDays.entrySet().stream()
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    default double sumSegments(List<Segment> segments, Function<Segment, Boolean> filter, FromLong millisToHours) {
        return segments
                .stream()
                .filter(filter::apply)
                .map(Segment::getDuration)
                .map(millisToHours::convert)
                .mapToDouble(d -> d)
                .sum();
    }

    default Map<String, Double> getExpectedDays(Instant start, Instant end) {
        double workingDays = 0.;
        double holidayDays = 0.;
        Date currentDate = start.toDate();
        Date lastDate = end.toDate();
        while (currentDate.before(lastDate) || currentDate.equals(lastDate)) {
            if (isHoliday(currentDate)) {
                holidayDays += 1;
            } else {
                workingDays += 1;
            }
            currentDate = nextDate(currentDate);
        }
        return Map.of("expectedWorkingDays", workingDays, "expectedHolidayDays", holidayDays);
    }

    default Date nextDate(Date date) {
        // TODO clean mess
        java.time.Instant nextInstant = date.toInstant().plus(1, ChronoUnit.DAYS);
        LocalDate localDate = nextInstant.atZone(ZoneId.of("UTC")).toLocalDate();
        return new Date(localDate.getYear(), localDate.getMonthValue() - 1, localDate.getDayOfMonth());
    }

    default boolean isHoliday(Date currentDate) {
        DayOfWeek dayOfWeek = currentDate.toInstant().atZone(ZoneId.of("UTC")).getDayOfWeek();
        return dayOfWeek.equals(DayOfWeek.SATURDAY) || dayOfWeek.equals(DayOfWeek.SUNDAY);
    }

    List<Segment> getBetween(Instant start, Instant end);

    Segment getLast();

    Segment append(Segment segment);

    Segment replaceLast(Segment segment);

    Segment delete(int segmentId);
}

interface FromLong {
    double convert(long value);
}