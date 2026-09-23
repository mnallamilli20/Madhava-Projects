package tables.course;

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
public class BrendenlCourseSystemTest {

    @LocalServerPort
    int port;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.registerParser("text/plain", io.restassured.parsing.Parser.JSON);
    }

    @Test
    public void createAndVerifyCourseTest() {
        String uniName = "Test University " + System.currentTimeMillis();
        Map<String, Object> uniJson = new HashMap<>();
        uniJson.put("name", uniName);

        given()
                .contentType(ContentType.JSON)
                .body(uniJson)
                .post("/university")
                .then()
                .statusCode(200);

        Long universityId = ((Number) given()
                .pathParam("name", uniName)
                .when()
                .get("/university/name/{name}")
                .then()
                .statusCode(200)
                .extract()
                .path("university_id")).longValue();

        String courseName = "System Testing 101";
        String courseCode = "ST" + System.currentTimeMillis();

        Map<String, Object> universityRef = new HashMap<>();
        universityRef.put("university_id", universityId);

        Map<String, Object> courseJson = new HashMap<>();
        courseJson.put("name", courseName);
        courseJson.put("course_code", courseCode);
        courseJson.put("subject", "COMPUTER_SCIENCE");
        courseJson.put("university", universityRef);

        given()
                .contentType(ContentType.JSON)
                .body(courseJson)
                .when()
                .post("/courses") //
                .then()
                .statusCode(200)
                .body("message", is("success"));

        given()
                .pathParam("course_code", courseCode)
                .when()
                .get("/courses/code/{course_code}")
                .then()
                .statusCode(200)
                .body("name", is(courseName))
                .body("course_code", is(courseCode));
    }

    @Test
    public void updateCourseTest() {
        String uniName = "UpdateTestUni" + System.currentTimeMillis();
        Map<String, Object> uniJson = new HashMap<>();
        uniJson.put("name", uniName);

        given().contentType(ContentType.JSON).body(uniJson).post("/university");

        Long universityId = ((Number) given()
                .pathParam("name", uniName)
                .get("/university/name/{name}")
                .then().extract().path("university_id")).longValue();

        String courseCode = "UP" + System.currentTimeMillis();
        Map<String, Object> universityRef = new HashMap<>();
        universityRef.put("university_id", universityId);

        Map<String, Object> courseJson = new HashMap<>();
        courseJson.put("name", "Old Name");
        courseJson.put("course_code", courseCode);
        courseJson.put("university", universityRef);

        given()
                .contentType(ContentType.JSON)
                .body(courseJson)
                .post("/courses")
                .then()
                .body("message", is("success"));

        long courseId = ((Number) given()
                .pathParam("course_code", courseCode)
                .get("/courses/code/{course_code}")
                .then()
                .extract()
                .path("course_id")).longValue();

        courseJson.put("name", "Newly Updated Course Name");

        given()
                .pathParam("id", courseId)
                .contentType(ContentType.JSON)
                .body(courseJson)
                .when()
                .put("/courses/{id}")
                .then()
                .statusCode(200)
                .body("message", is("success"));

        given()
                .pathParam("id", courseId)
                .when()
                .get("/courses/{id}")
                .then()
                .statusCode(200)
                .body("name", is("Newly Updated Course Name"));
    }

    @Test
    public void deleteCourseTest() {
        String uniName = "DeleteTestUni" + System.currentTimeMillis();
        Map<String, Object> uniJson = new HashMap<>();
        uniJson.put("name", uniName);

        given().contentType(ContentType.JSON).body(uniJson).post("/university");

        Long universityId = ((Number) given()
                .pathParam("name", uniName)
                .get("/university/name/{name}")
                .then().extract().path("university_id")).longValue();

        String deleteCode = "DEL" + System.currentTimeMillis();
        Map<String, Object> courseJson = new HashMap<>();
        courseJson.put("name", "Delete Me");
        courseJson.put("course_code", deleteCode);
        Map<String, Object> uniRef = new HashMap<>();
        uniRef.put("university_id", universityId);
        courseJson.put("university", uniRef);

        given().contentType(ContentType.JSON).body(courseJson).post("/courses");

        Long id = ((Number) given()
                .pathParam("course_code", deleteCode)
                .when()
                .get("/courses/code/{course_code}")
                .then()
                .extract()
                .path("course_id")).longValue();

        given()
                .pathParam("id", id)
                .when()
                .delete("/courses/{id}")
                .then()
                .statusCode(200)
                .body("message", is("success")); // CourseController returns success string

        given()
                .pathParam("id", id)
                .when()
                .get("/courses/{id}")
                .then()
                .statusCode(200) // Your controller returns null which Spring treats as 200 OK with empty body
                .body(is(emptyOrNullString()));
    }
}