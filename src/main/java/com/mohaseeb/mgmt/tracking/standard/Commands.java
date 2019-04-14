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

package com.mohaseeb.mgmt.tracking.standard;

import com.mohaseeb.mgmt.tracking.application.TrackingService;
import com.mohaseeb.mgmt.tracking.domain.Segment;
import org.joda.time.Instant;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.standard.ValueProviderSupport;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Example commands for the Shell 2 Standard resolver.
 *
 * @author Eric Bottard
 */
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
    public void day() {
        // get today start
        LocalDateTime todayStart = new LocalDateTime();
        todayStart.withHourOfDay(0)
                .withMinuteOfHour(0)
                .withSecondOfMinute(0)
                .withMillisOfSecond(0);

        // get tomorrow start
        LocalDateTime tomorrowStart = todayStart.plusDays(1);


        System.out.printf("Today's total: %.2f hours\n", service.hoursBetween(
                todayStart.toDateTime().toInstant(),
                tomorrowStart.toDateTime().toInstant()

        ));
    }

    /*
    @ShellMethod(value = "show current week days")
    public void week(@ShellOption(defaultValue = NOTSET) String weekNo) {
        System.out.println("close the last open episode");
    }


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

/**
 * A {@link org.springframework.shell.standard.ValueProvider} that emits values with special characters
 * (quotes, escapes, <em>etc.</em>)
 *
 * @author Eric Bottard
 */
@Component
class CurrentTimestampProvider extends ValueProviderSupport {

    @Override
    public List<CompletionProposal> complete(MethodParameter parameter, CompletionContext completionContext, String[] hints) {
        return Collections.singletonList(new CompletionProposal(Instant.now().toString()));
    }
}
