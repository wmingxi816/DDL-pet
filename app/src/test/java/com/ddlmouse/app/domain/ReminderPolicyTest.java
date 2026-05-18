package com.ddlmouse.app.domain;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class ReminderPolicyTest {
    @Test
    public void shortDeadlinesKeepOneUsefulReminder() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 18, 8, 0);
        LocalDateTime deadline = LocalDateTime.of(2026, 5, 18, 13, 0);

        assertEquals(
                Collections.singletonList(LocalDateTime.of(2026, 5, 18, 12, 0)),
                ReminderPolicy.INSTANCE.defaultReminderTimes(now, deadline));
    }

    @Test
    public void threeDayDeadlinesUseOneDayAndTwoHourWarnings() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 18, 8, 0);
        LocalDateTime deadline = LocalDateTime.of(2026, 5, 20, 21, 0);

        assertEquals(
                Arrays.asList(
                        LocalDateTime.of(2026, 5, 19, 20, 0),
                        LocalDateTime.of(2026, 5, 20, 19, 0)),
                ReminderPolicy.INSTANCE.defaultReminderTimes(now, deadline));
    }

    @Test
    public void distantDeadlinesReceiveMoreReminderCheckpoints() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 18, 8, 0);
        LocalDateTime deadline = LocalDateTime.of(2026, 6, 27, 21, 0);

        assertEquals(
                Arrays.asList(
                        LocalDateTime.of(2026, 5, 28, 20, 0),
                        LocalDateTime.of(2026, 6, 13, 20, 0),
                        LocalDateTime.of(2026, 6, 20, 20, 0),
                        LocalDateTime.of(2026, 6, 24, 20, 0),
                        LocalDateTime.of(2026, 6, 26, 20, 0),
                        LocalDateTime.of(2026, 6, 27, 19, 0)),
                ReminderPolicy.INSTANCE.defaultReminderTimes(now, deadline));
    }
}

