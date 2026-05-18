package com.ddlmouse.app.domain;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.Test;

public class SchedulePolicyTest {
    @Test
    public void businessDateStartsAtFiveInTheMorning() {
        assertEquals(
                LocalDate.of(2026, 5, 17),
                SchedulePolicy.INSTANCE.businessDate(LocalDateTime.of(2026, 5, 18, 4, 59)));
        assertEquals(
                LocalDate.of(2026, 5, 18),
                SchedulePolicy.INSTANCE.businessDate(LocalDateTime.of(2026, 5, 18, 5, 0)));
    }

    @Test
    public void periodKeysMatchTheTaskModule() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 18, 8, 0);

        assertEquals("2026-05-18", SchedulePolicy.INSTANCE.periodKey(TaskModule.DAILY, now, 9));
        assertEquals("2026-W21", SchedulePolicy.INSTANCE.periodKey(TaskModule.WEEKLY, now, 9));
        assertEquals("2026-05", SchedulePolicy.INSTANCE.periodKey(TaskModule.MONTHLY, now, 9));
        assertEquals("single-9", SchedulePolicy.INSTANCE.periodKey(TaskModule.PROJECT, now, 9));
        assertEquals("single-9", SchedulePolicy.INSTANCE.periodKey(TaskModule.TODO, now, 9));
    }
}

