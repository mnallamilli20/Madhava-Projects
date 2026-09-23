package tables.schedule;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@Tag(name = "Schedule Management", description = "Endpoints for creating and managing student course schedules")
public class ScheduleController {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Operation(summary = "Get all schedules")
    @GetMapping
    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    @Operation(summary = "Get schedule by ID")
    @GetMapping("/{id}")
    public Schedule getScheduleById(@PathVariable Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found"));
    }

    @Operation(summary = "Get schedules by User ID")
    @GetMapping("/user/{userId}")
    public List<Schedule> getSchedulesByUser(@PathVariable Long userId) {
        return scheduleRepository.findByUser_UserId(userId);
    }

    @Operation(summary = "Create a new schedule")
    @PostMapping
    public Schedule createSchedule(@RequestBody Schedule schedule) {
        return scheduleRepository.save(schedule);
    }

    @Operation(summary = "Update a schedule")
    @PutMapping("/{id}")
    public Schedule updateSchedule(@PathVariable Long id, @RequestBody Schedule scheduleDetails) {
        return scheduleRepository.findById(id).map(existingSchedule -> {
            existingSchedule.setName(scheduleDetails.getName());
            existingSchedule.setUser(scheduleDetails.getUser());
            existingSchedule.setUniversity(scheduleDetails.getUniversity());
            existingSchedule.setCourses(scheduleDetails.getCourses());
            return scheduleRepository.save(existingSchedule);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found"));
    }

    @Operation(summary = "Partially update schedule details", description = "Updates only the fields provided in the request body")
    @PatchMapping("/{id}")
    public Schedule patchSchedule(@PathVariable Long id, @RequestBody Schedule partialSchedule) {
        return scheduleRepository.findById(id).map(existingSchedule -> {

            if (partialSchedule.getName() != null) {
                existingSchedule.setName(partialSchedule.getName());
            }
            if (partialSchedule.getUser() != null) {
                existingSchedule.setUser(partialSchedule.getUser());
            }
            if (partialSchedule.getUniversity() != null) {
                existingSchedule.setUniversity(partialSchedule.getUniversity());
            }
            if (partialSchedule.getCourses() != null && !partialSchedule.getCourses().isEmpty()) {
                existingSchedule.setCourses(partialSchedule.getCourses());
            }

            return scheduleRepository.save(existingSchedule);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found"));
    }

    @Operation(summary = "Delete a schedule")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        if (!scheduleRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found");
        }
        scheduleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}