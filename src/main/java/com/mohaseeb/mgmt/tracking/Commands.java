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

import java.sql.Time;
import java.util.Collections;
import java.util.List;

@ShellComponent()
public class Commands {
    private final static String NOTSET = "not set";

    @Autowired
    private TrackingService service;

    @ShellMethod(value = "Starts a an episode")
    public void start(@ShellOption(valueProvider = CurrentTimestampProvider.class) String when) {
        Segment segment = service.start(parseInstant(when));
        System.out.println("Started: \n" + segment);
    }

    @ShellMethod(value = "Ends the last episode")
    public void end(@ShellOption(valueProvider = CurrentTimestampProvider.class) String when) {
        Segment segment = service.end(parseInstant(when));
        System.out.println("Ended: \n" + segment);
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

    @ShellMethod(value = "show current day")
    public Table today() {
        Instant todayStart = TimeUtils.today();
        Instant tomorrowStart = TimeUtils.nextDay(todayStart);
        List<Segment> segments = service.getBetween(todayStart, tomorrowStart);

        int height = segments.size();
        int width = 3;
        String[][] data = new String[height + 2][width];
        data[0][0] = "Start";
        data[0][1] = "End";
        data[0][2] = "Minutes";
        double total = 0;
        for (int i = 1; i <= height; i++) {
            Segment s = segments.get(i - 1);
            double minutes = s.getDuration() / (1000. * 60.);
            total += minutes;
            data[i][0] = TimeUtils.localDateTimeFormat(s.getStart());
            data[i][1] = TimeUtils.localDateTimeFormat(s.getEnd());
            data[i][2] = String.format("%.2f", minutes);
        }
        data[height + 1][0] = "Total";
        data[height + 1][1] = "";
        data[height + 1][2] = String.format("%.2f Hours", total / 60.);


        return renderTable(data);
    }


    @ShellMethod(value = "show current week days")
    public Table week() {
        return computeDayTotals(TimeUtils.monday(TimeUtils.today()), 7);
    }


    @ShellMethod(value = "show current month days")
    public Table month() {
        return computeDayTotals(TimeUtils.firstDayOfMonth(), 31);
    }

    @ShellMethod(value = "show n days starting from specific day")
    public Table days(
            @ShellOption(valueProvider = CurrentDateProvider.class) String from,
            @ShellOption(defaultValue = "30") int nDays
    ) {
        return computeDayTotals(parseInstant(from), nDays);
    }

    private Table computeDayTotals(Instant day, int nDays) {
        double totalHours = 0;

        String[][] data = new String[nDays + 2][3];
        data[0][0] = "Day";
        data[0][1] = "Date";
        data[0][2] = "Hours";
        for (int i = 1; i <= nDays; i++) {
            double dayHours = getDayHours(day);
            data[i][0] = TimeUtils.weekDay(day);
            data[i][1] = TimeUtils.localDateFormat(day);
            data[i][2] = String.format("%.2f", dayHours);
            totalHours += dayHours;
            day = TimeUtils.nextDay(day);
        }
        data[nDays + 1][0] = "All";
        data[nDays + 1][1] = "";
        data[nDays + 1][2] = String.format("%.2f", totalHours);
        return renderTable(data);
    }

    private double getDayHours(Instant dayStart) {
        return service.hoursBetween(dayStart, TimeUtils.nextDay(dayStart));
    }

    private static Table renderTable(String[][] data) {
        TableModel model = new ArrayTableModel(data);
        TableBuilder tableBuilder = new TableBuilder(model);
        return tableBuilder.addFullBorder(BorderStyle.fancy_light).build();
    }

    private static CellMatcher at(final int theRow, final int col) {
        return new CellMatcher() {
            @Override
            public boolean matches(int row, int column, TableModel model) {
                return row == theRow && column == col;
            }
        };
    }
/*
    @ShellMethod(value = "show current month weeks")
    public void month(@ShellOption(defaultValue = NOTSET) String monthNo) {
        System.out.println("close the last open episode");
    }

    @ShellMethod(value = "show current months")
    public void year(@ShellOption(defaultValue = NOTSET) String yearNo) {
        System.out.println("close the last open episode");
    }

*/
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
