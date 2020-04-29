package com.company;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.*;

// DONE by Artem Chebotarov as recruitment task by Orange company

public class Main {

    private static final LocalTime DURATION = LocalTime.parse("00:30");

    public static void main(String[] args) throws IOException {
        JSONObject calendar1JSON = getJSONCalendar(readFile("calendar1"));
        JSONObject calendar2JSON = getJSONCalendar(readFile("calendar2"));

        // Getting of lists of free time for both of calendars
        List<Interval> calendar1FreeTime = getFreeTime(calendar1JSON);
        List<Interval> calendar2FreeTime = getFreeTime(calendar2JSON);

        System.out.println(getAppropriateTime(calendar1FreeTime, calendar2FreeTime));
    }

    private static String readFile(String path) throws IOException {
        File file = new File(path);
        Scanner sc = new Scanner(file);

        String out = "";
        while (sc.hasNextLine())
            out += sc.nextLine() + "\n";

        return out;
    }

    // org.json - library for parsing String to JSON and operating on it
    private static JSONObject getJSONCalendar(String calendar) {
        return new JSONObject(calendar);
    }

    // Method that makes better comparing of times, it converts time (hh:mm) to minutes
    private static long toMinutes(LocalTime time) {
        return time.getHour()*60 + time.getMinute();
    }

    // Method gets free intervals of time for given calendar
    private static List<Interval> getFreeTime(JSONObject calendar) {
        JSONArray arr = calendar.getJSONArray("planned_meeting");
        List<Interval> plannedMeetings = new ArrayList<>();
        List<Interval> out = new ArrayList<>();

        for(int i = 0; i < arr.length(); i++) {
            plannedMeetings.add(new Interval(LocalTime.parse(arr.getJSONObject(i).getString("start")),
                    LocalTime.parse(arr.getJSONObject(i).getString("end"))));
        }

        for (int i = 0; i < plannedMeetings.size() - 1; i++) {
            if (toMinutes(plannedMeetings.get(i).getEnd()) != toMinutes(plannedMeetings.get(i+1).getStart()))
                out.add(new Interval(plannedMeetings.get(i).getEnd(), plannedMeetings.get(i+1).getStart()));
        }

        if (toMinutes(plannedMeetings.get(plannedMeetings.size()-1).getEnd()) !=
                toMinutes(LocalTime.parse(calendar.getJSONObject("working_hours").getString("end"))))
            out.add(new Interval(plannedMeetings.get(plannedMeetings.size()-1).getEnd(),
                    LocalTime.parse(calendar.getJSONObject("working_hours").getString("end"))));


        return out;
    }

    // This method return list of intervals in which can be organized meeting. This intervals fit to both of schedules

    //********************************* Explanation *************************************
    // 1. For each interval in calendar1FreeTime we check
    //    1a. Is start time of interval is between start time and and end time of some interval of calendar2FreeTime?
    //    1b. Is end time of interval is between start time and and end time of some interval of calendar2FreeTime?
    //    1c. Or may be start time of interval is equal to start time of some interval of calendar2FreeTime (the same for end time)?
    // 2. If some of this checks passed:
    //    We creating new Interval, where start time will be MAXIMAL time and end time will be MINIMAL time between
    //    intervals from calendar1FreeTime and calendar2FreeTime
    // 3. We check if new interval is counted on DURATION of possible meeting (endTime - startTime can not be less of DURATION)
    // 4. Finally, interval is added to list of appropriate times for meeting if all conditions are satisfied
    //***********************************************************************************

    private static List<Interval> getAppropriateTime(List<Interval> calendar1FreeTime, List<Interval> calendar2FreeTime) {
        List<Interval> out = new ArrayList<>();

        for (Interval interval1 : calendar1FreeTime) {
            for (Interval interval2 : calendar2FreeTime) {

                if((interval1.getStart().isAfter(interval2.getStart()) && interval1.getStart().isBefore(interval2.getEnd())) ||
                        (interval1.getEnd().isAfter(interval2.getStart()) && interval1.getEnd().isBefore(interval2.getEnd())) ||
                        toMinutes(interval1.getStart()) == toMinutes(interval2.getStart()) ||
                        toMinutes(interval1.getEnd()) == toMinutes(interval2.getEnd())) {
                    Interval newAppropriateTime = new Interval(interval1.getStart().isAfter(interval2.getStart()) ? interval1.getStart() : interval2.getStart(),
                            interval1.getEnd().isBefore(interval2.getEnd()) ? interval1.getEnd() : interval2.getEnd());

                    if (!(toMinutes(newAppropriateTime.getEnd()) - toMinutes(newAppropriateTime.getStart()) < toMinutes(DURATION)))
                        out.add(newAppropriateTime);
                }

            }
        }
        return out;
    }
}
