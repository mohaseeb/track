/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mohaseeb.mgmt.tracking;

import com.mohaseeb.mgmt.tracking.application.TrackingService;
import com.mohaseeb.mgmt.tracking.domain.Segment;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.standard.ValueProviderSupport;
import org.springframework.shell.table.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@ShellComponent()
public class Commands {
    private final static String NOTSET = "not set";

    @Autowired
    private TrackingService service;

    @ShellMethod(value = "Starts a an segment")
    public void start(
            @ShellOption(valueProvider = CurrentTimestampProvider.class) String when,
            @ShellOption(defaultValue = NOTSET) String note,
            @ShellOption(defaultValue = "0") String absent
    ) {
        Segment segment = service.start(
                parseInstant(when),
                absent.equals("1"),
                note.equals(NOTSET) ? null : note);
        System.out.println("Started: \n" + segment);
    }

    @ShellMethod(value = "Ends the last segment")
    public void end(
            @ShellOption(valueProvider = CurrentTimestampProvider.class) String when,
            @ShellOption(defaultValue = NOTSET) String note
    ) {
        Segment segment = service.end(parseInstant(when), note.equals(NOTSET) ? null : note);
        System.out.println("Ended: \n" + segment);
    }

    @ShellMethod(value = "Delete a segment by Id")
    public void delete(String id) {
        Segment segment = service.delete(Integer.parseInt(id));
        System.out.println("Deleted: \n" + segment);
    }

    private Instant parseInstant(String instantStr) {
        return instantStr.equals(NOTSET) ? Instant.now() : Instant.parse(instantStr);
    }

    @ShellMethod(value = "show all segments")
    public void all() {
        List<Segment> segments = service.getAll();
        showSegments(segments);
    }

    private void showSegments(List<Segment> segments) {
        for (Segment segment : segments) {
            System.out.println(segment);
        }
    }

    @ShellMethod(value = "Export segments to CSV")
    public void csv(
            @ShellOption(defaultValue = NOTSET) String month,
            @ShellOption(defaultValue = NOTSET) String path
    ) throws IOException {
        int monthNum = month.equals(NOTSET) ?
                -1 :
                Integer.valueOf(month);
        path = path.equals(NOTSET) ?
                String.format("/tmp/segments_%s_%s.csv", monthNum, TimeUtils.localDateTimeFormat(Instant.now())) :
                path;

        List<Segment> segments;
        if (monthNum == -1) {
            segments = service.getAll();
            System.out.println("exporting all time segments");
        } else {
            List<Instant> thisAndNextMonth = TimeUtils.thisAndNextMonthStarts(monthNum);
            segments = service.getBetween(thisAndNextMonth.get(0), thisAndNextMonth.get(1));
        }

        CsvWriter.write(segments, path);
        System.out.printf("written to: %s\n", path);
    }


    @ShellMethod(value = "show day")
    public Table day(@ShellOption(valueProvider = CurrentDateProvider.class) String day) {
        return showDaySegments(parseInstant(day));
    }

    @ShellMethod(value = "show current day")
    public Table today() {
        return showDaySegments(TimeUtils.today());
    }

    private Table showDaySegments(Instant day) {
        Instant tomorrowStart = TimeUtils.nextDay(day);
        List<Segment> segments = service.getBetween(day, tomorrowStart);

        int height = segments.size();
        int width = 6;
        String[][] data = new String[height + 2][width];
        data[0][0] = "Id";
        data[0][1] = "Start";
        data[0][2] = "End";
        data[0][3] = "Minutes";
        data[0][4] = "Absent";
        data[0][5] = "Notes";
        double workingHours = 0;
        double absentHours = 0;
        for (int i = 1; i <= height; i++) {
            Segment s = segments.get(i - 1);
            double minutes = s.getDuration() / (1000. * 60.);
            if (s.getAbsent() == 0) {
                workingHours += minutes / 60;
            } else {
                absentHours += minutes / 60;
            }
            data[i][0] = String.format("%s", s.getId());
            data[i][1] = TimeUtils.localDateTimeFormat(s.getStart());
            data[i][2] = TimeUtils.localDateTimeFormat(s.getEnd());
            data[i][3] = String.format("%.2f", minutes);
            data[i][4] = String.format("%s", s.getAbsent() == 1 ? "X" : "");
            data[i][5] = s.getNotes();
        }
        data[height + 1][0] = "Total";
        data[height + 1][2] = "";
        data[height + 1][3] = String.format(
                "working: %.2f, absent: %.2f, total: %.2f Hours",
                workingHours, absentHours,
                workingHours + absentHours);
        return renderTable(data);
    }

    @ShellMethod(value = "show day")
    public Table cday(@ShellOption(valueProvider = CurrentDateProvider.class) String day) {
        return showDaySegmentsConcise(parseInstant(day));
    }

    @ShellMethod(value = "show current day")
    public Table ctoday() {
        return showDaySegmentsConcise(TimeUtils.today());
    }

    private Table showDaySegmentsConcise(Instant day) {
        try {
            Instant tomorrowStart = TimeUtils.nextDay(day);
            List<Segment> segments = service.getBetween(day, tomorrowStart);

            Map<String, Segment> groupedSegments = new HashMap<>();

            for (Segment segment : segments) {
                String segmentNote = segment.getNotes();
                Segment noteSegment = groupedSegments.get(segmentNote);
                if (noteSegment == null) {
                    noteSegment = new Segment();
                    noteSegment.setNotes(segmentNote);
                    noteSegment.setStart(segment.getStart());
                    noteSegment.setEnd(segment.getEnd());
                    noteSegment.setDuration(segment.getDuration());
                    groupedSegments.put(segmentNote, noteSegment);
                } else {
                    noteSegment.setDuration(
                            noteSegment.getDuration() + segment.getDuration()
                    );
                    noteSegment.setStart(
                            noteSegment.getStart().isBefore(segment.getStart()) ?
                                    noteSegment.getStart() :
                                    segment.getStart()
                    );
                    noteSegment.setEnd(
                            noteSegment.getEnd().isAfter(segment.getEnd()) ?
                                    noteSegment.getEnd() :
                                    segment.getEnd()
                    );
                }
            }

            int height = groupedSegments.size();
            Object[] groupNames = groupedSegments.keySet().toArray();
            int width = 5;
            String[][] data = new String[height + 2][width];
            data[0][0] = "Earliest start";
            data[0][1] = "Latest end";
            data[0][2] = "Minutes";
            data[0][3] = "Absent";
            data[0][4] = "Notes";
            double total = 0;
            for (int i = 1; i <= height; i++) {
                Segment s = groupedSegments.get((String) groupNames[i - 1]);
                double minutes = s.getDuration() / (1000. * 60.);
                total += minutes;
                data[i][0] = TimeUtils.localDateTimeFormat(s.getStart());
                data[i][1] = TimeUtils.localDateTimeFormat(s.getEnd());
                data[i][2] = String.format("%.2f", minutes);
                data[i][3] = String.format("%s", s.getAbsent() == 1 ? "X" : " ");
                data[i][4] = s.getNotes();
            }
            data[height + 1][0] = "Total";
            data[height + 1][1] = "";
            data[height + 1][2] = String.format("%.2f Hours", total / 60.);

            return renderTable(data);
        } catch (Throwable e) {

            e.printStackTrace();
        }
        return null;
    }

    @ShellMethod(value = "show current week days")
    public Table week(@ShellOption(valueProvider = CurrentDateProvider.class) String dayInWeek) {
        Instant day = TimeUtils.monday(parseInstant(dayInWeek));
        return computeDayTotals(day, 7);
    }


    @ShellMethod(value = "show current month days")
    public Table month(@ShellOption(defaultValue = NOTSET) String month) {
        Instant firstDayOfMonth;
        if (month.equals(NOTSET)) firstDayOfMonth = TimeUtils.firstDayOfMonth();
        else firstDayOfMonth = TimeUtils.firstDayOfMonth(Integer.valueOf(month));

        int monthDays = TimeUtils.monthDays(
                firstDayOfMonth.toDateTime().getYear(),
                firstDayOfMonth.toDateTime().getMonthOfYear()
        );

        return computeDayTotals(firstDayOfMonth, monthDays);
    }

    @ShellMethod(value = "show n days starting from specific day")
    public Table days(
            @ShellOption(valueProvider = CurrentDateProvider.class) String from,
            @ShellOption(defaultValue = "30") int nDays
    ) {
        return computeDayTotals(parseInstant(from), nDays);
    }

    private Table computeDayTotals(Instant day, int nDays) {
        double workingHoursTotal = 0;
        double absentHoursTotal = 0;
        double expectedWorkingDaysTotal = 0;
        double expectedHolidayDaysTotal = 0;

        String[][] data = new String[nDays + 3][6];
        data[0][0] = "Day";
        data[0][1] = "Date";
        data[0][2] = "Working Hours";
        data[0][3] = "Absent Hours";
        data[0][4] = "Total Hours";
        data[0][5] = "Notes";
        for (int i = 1; i <= nDays; i++) {
            Map<String, Serializable> daySummary = getDaySummary(day);
            double workingHours = (double) daySummary.get("workingHours");
            double absentHours = (double) daySummary.get("absentHours");
            String notes = (String) daySummary.get("notes");
            data[i][0] = TimeUtils.weekDay(day);
            data[i][1] = TimeUtils.localDateFormat(day);
            data[i][2] = String.format("%.2f", workingHours);
            data[i][3] = String.format("%.2f", absentHours);
            data[i][4] = String.format("%.2f", workingHours + absentHours);
            data[i][5] = notes;

            workingHoursTotal += workingHours;
            absentHoursTotal += absentHours;
            expectedWorkingDaysTotal += (double) daySummary.get("expectedWorkingDays");
            expectedHolidayDaysTotal += (double) daySummary.get("expectedHolidayDays");

            day = TimeUtils.nextDay(day);
        }
        data[nDays + 1][0] = "All";
        data[nDays + 1][1] = "";
        data[nDays + 1][2] = String.format("%.2f (%.2f days)", workingHoursTotal, workingHoursTotal / 8);
        data[nDays + 1][3] = String.format("%.2f (%.2f days)", absentHoursTotal, absentHoursTotal / 8);
        double totalHours = workingHoursTotal + absentHoursTotal;
        data[nDays + 1][4] = String.format("%.2f (%.2f days)", totalHours, totalHours / 8);

        data[nDays + 2][0] = "Balance";
        data[nDays + 2][1] = "";
        data[nDays + 2][2] = String.format("Accounted %.2f days", totalHours / 8);
        data[nDays + 2][3] = String.format("Expected %.2f days", expectedWorkingDaysTotal);
        data[nDays + 2][4] = String.format("Remaining %.2f days", expectedWorkingDaysTotal - totalHours / 8);
        return renderTable(data);
    }

    private Map<String, Serializable> getDaySummary(Instant dayStart) {
        return service.summaryBetween(dayStart, TimeUtils.nextDay(dayStart));
    }

    private static Table renderTable(String[][] data) {
        TableModel model = new ArrayTableModel(data);
        TableBuilder tableBuilder = new TableBuilder(model);
        return tableBuilder.addFullBorder(BorderStyle.fancy_light).build();
    }
}


@Component
class CurrentTimestampProvider extends ValueProviderSupport {

    @Override
    public List<CompletionProposal> complete(MethodParameter parameter, CompletionContext completionContext, String[] hints) {
        return Collections.singletonList(new CompletionProposal(
                Instant.now().toDateTime().toLocalDateTime().toString()
        ));
    }
}


@Component
class CurrentDateProvider extends ValueProviderSupport {

    @Override
    public List<CompletionProposal> complete(MethodParameter parameter, CompletionContext completionContext, String[] hints) {
        return Collections.singletonList(new CompletionProposal(
                Instant.now().toDateTime().toLocalDate().toString()
        ));
    }
}
