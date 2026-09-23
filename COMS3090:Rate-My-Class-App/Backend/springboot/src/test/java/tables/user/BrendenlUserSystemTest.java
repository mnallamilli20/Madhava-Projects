package tables.user;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import java.util.HashMap;
import java.util.Map;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BrendenlUserSystemTest {
    @LocalServerPort
    int port;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    public void createUserAndVerifyTest() {
        String uniqueName = "SystemTest1" + System.currentTimeMillis();

        Map<String, Object> userJson = new HashMap<>();
        userJson.put("username", uniqueName);
        userJson.put("email", uniqueName + "@test.com");
        userJson.put("pass_hash", "password123");
        userJson.put("role", "USER");

        long userId = ((Number) given()
                .contentType(ContentType.JSON)
                .body(userJson)
                .when()
                .post("/users")
                .then()
                .statusCode(200)
                .extract()
                .path("user_id")).longValue();

        given()
                .pathParam("id", userId)
                .when()
                .get("/users/{id}")
                .then()
                .statusCode(200)
                .body("username", is(uniqueName));
    }

    @Test
    public void loginSystemTest() {
        String username = "SystemTest2" + System.currentTimeMillis();
        String password = "testPassword";

        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("email", username + "@login.com");
        user.put("pass_hash", password);
        user.put("role", "USER");

        given().contentType(ContentType.JSON).body(user).post("/users");

        Map<String, String> loginCredentials = new HashMap<>();
        loginCredentials.put("username", username);
        loginCredentials.put("pass_hash", password);

        given()
                .contentType(ContentType.JSON)
                .body(loginCredentials)
                .when()
                .post("/users/login")
                .then()
                .statusCode(200)
                .body("username", is(username));

        loginCredentials.put("pass_hash", "wrong_pass");
        given()
                .contentType(ContentType.JSON)
                .body(loginCredentials)
                .when()
                .post("/users/login")
                .then()
                .statusCode(401);
    }

    @Test
    public void updateUserTest() {
        String name = "SystemTest3" + System.currentTimeMillis();
        Map<String, Object> user = new HashMap<>();
        user.put("username", name);
        user.put("email", name + "@test.com");
        user.put("pass_hash", "pass");
        user.put("role", "USER");

        long id = ((Number) given()
                .contentType(ContentType.JSON)
                .body(user)
                .post("/users")
                .path("user_id")).longValue();

        user.put("username", name + "New");

        given()
                .pathParam("id", id)
                .contentType(ContentType.JSON)
                .body(user)
                .when()
                .put("/users/{id}")
                .then()
                .statusCode(200)
                .body(containsString("success"));
    }

    @Test
    public void deleteUserTest() {
        String name = "SystemTest4" + System.currentTimeMillis();
        Map<String, Object> user = new HashMap<>();
        user.put("username", name);
        user.put("email", name + "@test.com");
        user.put("pass_hash", "pass");
        user.put("role", "USER");

        long id = ((Number) given()
                .contentType(ContentType.JSON)
                .body(user)
                .post("/users")
                .path("user_id")).longValue();

        given()
                .pathParam("id", id)
                .when()
                .delete("/users/{id}")
                .then()
                .statusCode(200)
                .body("deleted_id", is((int)id));

        given()
                .pathParam("id", id)
                .when()
                .get("/users/{id}")
                .then()
                .body(is(emptyOrNullString()));
    }
}
