package com.example.hobbiesmanagment.Scheduler;

import com.example.hobbiesmanagment.Service.WaitlistService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component // מגדיר את המחלקה כרכיב ש-Spring מנהל
public class WaitlistScheduler {

    private final WaitlistService waitlistService;

    public WaitlistScheduler(WaitlistService waitlistService) {
        this.waitlistService = waitlistService;
    }

    //רץ פעם בדקה משמונה בבוקר עד עשר בלילה
    @Scheduled(cron = "0 * 8-22 * * *")
    public void checkWaitlistExpirations() {

        waitlistService.processExpirationsAndNotifyNext();
    }
}