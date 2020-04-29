package com.company;

import java.time.LocalTime;

public class Interval {
    private LocalTime start;
    private LocalTime end;

    public Interval(LocalTime start, LocalTime end) {
        this.start = start;
        this.end = end;
    }

    public LocalTime getStart() {
        return start;
    }

    public LocalTime getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "[\"" + start + "\", \"" + end + "\"]";
    }
}
