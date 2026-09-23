package tables.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing User accounts.
 * Handles user registration, profile updates, and identity lookups.
 */
@RestController
@RequestMapping(path = "/users")
@Tag(name = "User Controller", description = "Operations for user account management")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    /**
     * Returns a list of all registered users.
     */
    @Operation(summary = "Get all users")
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Fetches a specific user by their unique primary key.
     */
    @Operation(summary = "Get user by ID")
    @GetMapping(path = "/{id}")
    public User getUserById(@Parameter(description = "User primary key") @PathVariable Long id) {
        return userRepository.findById(id).orElse(null);
    }

    /**
     * Look up a user by their unique username.
     */
    @Operation(summary = "Get user by username", description = "Finds a user based on their login handle")
    @GetMapping(path = "/username/{username}")
    public User getUserByUsername(@PathVariable String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Look up a user by their registered email address.
     */
    @Operation(summary = "Get user by email")
    @GetMapping(path = "/email/{email}")
    public User getUserByEmail(@PathVariable String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Registers a new user in the system.
     * @return The newly created User object including its generated ID.
     */
    @Operation(summary = "Create a user", description = "Registers a new user and returns the saved object")
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    @Operation(summary = "login with existing user", description = "finds user by username and checks if password matches")
    @PostMapping(path = "/login")
    public User login(@RequestBody User login) {
        User existingUser = userRepository.findByUsername(login.getUsername());
        if (existingUser != null && existingUser.getPassHash().equals(login.getPassHash())) {
            return existingUser;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }

    /** Updates an existing user's profile and credentials. */
    @Operation(summary = "Update user details", description = "Updates username, email, password, role, and university")
    @ApiResponse(responseCode = "200", description = "Success or failure JSON message")
    @PutMapping(path = "/{id}")
    public String updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        try {
            User existingUser = userRepository.findById(id).orElse(null);

            if (existingUser == null) {
                return failure;
            }

            existingUser.setUsername(userDetails.getUsername());
            existingUser.setEmail(userDetails.getEmail());
            existingUser.setPassHash(userDetails.getPassHash());
            existingUser.setRole(userDetails.getRole());
            existingUser.setUniversity(userDetails.getUniversity());

            userRepository.save(existingUser);
            return success;
        } catch (Exception e) {
            return failure;
        }
    }

    /**
     * Updates a user in the system.
     * @return The updated User object .
     */
    @Operation(summary = "Partially update user details", description = "Updates only the fields provided in the request body")
    @PatchMapping(path = "/{id}")
    public User patchUser(@PathVariable Long id, @RequestBody User partialUser) {
        return userRepository.findById(id).map(existingUser -> {

            if (partialUser.getUsername() != null) {
                existingUser.setUsername(partialUser.getUsername());
            }
            if (partialUser.getEmail() != null) {
                existingUser.setEmail(partialUser.getEmail());
            }
            if (partialUser.getPassHash() != null) {
                existingUser.setPassHash(partialUser.getPassHash());
            }
            if (partialUser.getRole() != null) {
                existingUser.setRole(partialUser.getRole());
            }
            if (partialUser.getUniversity() != null) {
                existingUser.setUniversity(partialUser.getUniversity());
            }

            return userRepository.save(existingUser);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    /** * Permanently deletes a user from the system.
     * @return A map containing the ID of the deleted user.
     */
    @Operation(summary = "Delete a user")
    @DeleteMapping(path = "/{id}")
    public Map<String, Long> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        Map<String, Long> response = new HashMap<>();
        response.put("deleted_id", id);
        return response;
    }
}