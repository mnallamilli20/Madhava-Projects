package tables.course;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controller for managing Course records and their university associations.
 */
@RestController
@RequestMapping(path = "/courses")
@Tag(name = "Course Controller", description = "Endpoints for creating, fetching, and updating courses")
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    /** Fetches all courses from the database. */
    @Operation(summary = "List all courses", description = "Returns a list of all available courses")
    @GetMapping
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    /** Fetches a single course by its unique database ID. */
    @Operation(summary = "Get course by ID")
    @GetMapping(path = "/{id}")
    public Course getCourseById(@Parameter(description = "Primary key ID") @PathVariable Long id) {
        return courseRepository.findById(id).orElse(null);
    }

    /** Fetches a course by its specific academic code (e.g., CS309). */
    @Operation(summary = "Get course by code", description = "Lookup using unique course code like 'CS101'")
    @GetMapping(path = "/code/{course_code}")
    public Course getCourseByCode(@PathVariable String course_code) {
        return courseRepository.findByCourseCode(course_code);
    }

    /** Lists all courses offered by a specific university. */
    @Operation(summary = "Get courses by university ID")
    @GetMapping(path = "/university/{university_id}")
    public List<Course> getCoursesByUniversity(@PathVariable Long university_id) {
        return courseRepository.findByUniversity_UniversityId(university_id);
    }

    /** Updates an existing course record. Returns success/failure JSON. */
    @Operation(summary = "Update a course", description = "Updates name, code, subject, and description")
    @ApiResponse(responseCode = "200", description = "Successfully updated or record not found (failure message)")
    @PutMapping(path = "/{id}")
    public String updateCourse(@PathVariable Long id, @RequestBody Course courseDetails) {
        try {
            Course existingCourse = courseRepository.findById(id).orElse(null);

            if (existingCourse == null) {
                return failure;
            }
            existingCourse.setName(courseDetails.getName());
            existingCourse.setCourseCode(courseDetails.getCourseCode());
            existingCourse.setSubject(courseDetails.getSubject());
            existingCourse.setDescription(courseDetails.getDescription());

            courseRepository.save(existingCourse);
            return success;
        } catch (Exception e) {
            return failure;
        }
    }

    /** Creates a new course record. */
    @Operation(summary = "Create a new course")
    @PostMapping
    public String createCourse(@RequestBody Course course) {
        try {
            courseRepository.save(course);
            return success;
        } catch (Exception e) {
            return failure;
        }
    }

    /** Deletes a course by ID. */
    @Operation(summary = "Delete a course")
    @DeleteMapping(path = "/{id}")
    public String deleteCourse(@PathVariable Long id) {
        try {
            courseRepository.deleteById(id);
            return success;
        } catch (Exception e) {
            return failure;
        }
    }
}