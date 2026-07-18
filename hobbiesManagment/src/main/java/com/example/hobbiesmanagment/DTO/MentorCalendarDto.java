package com.example.hobbiesmanagment.DTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MentorCalendarDto {


    private Set<String> availableDays;


    private List<String> bookedDates;
}