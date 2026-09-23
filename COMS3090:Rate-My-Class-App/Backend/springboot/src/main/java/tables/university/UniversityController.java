package tables.university;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tables.user.User;
import tables.user.UserRepository;

import java.util.List;

/**
 * Controller for managing University.
 */
@RestController
@RequestMapping(path = "/university")
@Tag(name = "University Controller", description = "Management of university institutional data")
public class UniversityController {

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UniversityService universityService;


    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    /** Retrieves all registered universities. */
    @Operation(summary = "Get all universities")
    @GetMapping
    public List<University> getAllUniversities() {
        return universityRepository.findAll();
    }

    /** Retrieves a specific university by its database ID. */
    @Operation(summary = "Get university by ID")
    @GetMapping(path = "/{id}")
    public University getUniversityById(@Parameter(description = "Primary key ID") @PathVariable Long id) {
        return universityRepository.findById(id).orElse(null);
    }

    /** Retrieves a specific university by its unique name. */
    @Operation(summary = "Get university by name")
    @GetMapping(path = "/name/{name}")
    public University getUniversityByName(@PathVariable String name) {
        return universityRepository.findByName(name);
    }

    /** Creates a new university record. Handles Editor association correctly. */
    @Operation(summary = "Create a university")
    @ApiResponse(responseCode = "200", description = "Record created or error occurred")
    @PostMapping
    public String createUniversity(@RequestBody University university) {
        try {
            if (university.getEditor() != null && university.getEditor().getUserId() != null) {
                User managedEditor = userRepository.findById(university.getEditor().getUserId()).orElse(null);
                if (managedEditor == null) {
                    return failure;
                }
                university.setEditor(managedEditor);
            }
            universityRepository.save(university);
            return success;
        } catch (Exception e) {
            e.printStackTrace();
            return failure;
        }
    }

    @PostMapping(path = "/signup")
    public String universitySignup(@RequestBody UniversitySignupRequest request) {
        try {
            universityService.registerUniversity(request.getUniversity(), request.getUser());
            return success;
        } catch (RuntimeException e) {
            return failure;
        }
    }

    /** Updates university details */
    @Operation(summary = "Update a university")
    @PutMapping(path = "/{id}")
    public String updateUniversity(@PathVariable Long id, @RequestBody University details) {
        try {
            University existingUniversity = universityRepository.findById(id).orElse(null);
            if (existingUniversity == null) {
                return failure;
            }

            existingUniversity.setName(details.getName());
            existingUniversity.setLocation(details.getLocation());
            existingUniversity.setWebsite(details.getWebsite());
            existingUniversity.setDescription(details.getDescription());
            existingUniversity.setLogo_url(details.getLogo_url());

            if (details.getEditor() != null && details.getEditor().getUserId() != null) {
                User managedEditor = userRepository.findById(details.getEditor().getUserId()).orElse(null);
                if (managedEditor != null) {
                    existingUniversity.setEditor(managedEditor);
                }
            }

            universityRepository.save(existingUniversity);
            return success;
        } catch (Exception e) {
            e.printStackTrace();
            return failure;
        }
    }

    /** Removes a university record. */
    @Operation(summary = "Delete a university")
    @DeleteMapping(path = "/{id}")
    public String deleteUniversity(@PathVariable Long id) {
        try {
            universityRepository.deleteById(id);
            return success;
        } catch (Exception e) {
            e.printStackTrace();
            return failure;
        }
    }
}