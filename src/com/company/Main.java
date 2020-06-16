package com.company;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.*;

// DONE by Artem Chebotarov as recruitment task by Orange

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

        StringBuilder out = new StringBuilder();
        while (sc.hasNextLine())
            out.append(sc.nextLine()).append("\n");

        return out.toString();
    }

    // org.json - library for parsing String to JSON and operating on it
    // org.json - biblioteka do parsowania String do JSON i działania na nim
    private static JSONObject getJSONCalendar(String calendar) {
        return new JSONObject(calendar);
    }

    // Method that makes better comparing of times, it converts time (hh:mm) to minutes
    // Metoda, dla lepszego porównania czasu, konwertuje czas (gg:mm) na minuty
    private static long toMinutes(LocalTime time) {
        return time.getHour()*60 + time.getMinute();
    }

    // Method gets free intervals of time for given calendar
    // Metoda otrzymuje wolne przedziały czasu dla danego kalendarza
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

    //********************************* Explanation (English) *************************************
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
    //********************************* Wyjaśnienie (Polski) *************************************
    // 1. Dla każdego interwału w calendar1FreeTime sprawdzamy
    //    1a. a. Czy czas rozpoczęcia interwału jest między czasem rozpoczęcia a czasem zakończenia jednego z interwałów calendar2FreeTime?
    //    1b. Czy czas zakończenia interwału jest między czasem rozpoczęcia a czasem zakończenia jakiegoś interwału calendar2FreeTime?
    //    1c. A może czas rozpoczęcia interwału jest równy czasowi rozpoczęcia jakiegoś interwału calendar2FreeTime (taki sam dla czasu zakończenia)?
    // 2. Jeśli niektóre z tych warunków przeszli:
    //    Tworzymy nowy Interwał, w którym czas rozpoczęcia będzie MAKSYMALNY czas, a czas zakończenia będzie MINIMALNY czas pomiędzy
    //    interwałami z calendar1FreeTime i calendar2FreeTime
    // 3. Sprawdzamy, czy nowy interwał liczony jest w CZASIE możliwego spotkania (endTime - startTime nie może być mniejszy niż DURATION)
    // 4. Na koniec dodawany jest odstęp do listy odpowiednich czasów do spełnienia, jeśli wszystkie warunki są spełnione
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