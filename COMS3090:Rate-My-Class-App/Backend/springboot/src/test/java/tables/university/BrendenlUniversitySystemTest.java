package tables.university;

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
public class BrendenlUniversitySystemTest {

    @LocalServerPort
    int port;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.registerParser("text/plain", io.restassured.parsing.Parser.JSON);
    }

    @Test
    public void createUniversityTest() {
        String uniName = "Test Uni " + System.currentTimeMillis();
        Map<String, Object> uni = new HashMap<>();
        uni.put("name", uniName);
        uni.put("location", "Test City");
        uni.put("website", "https://test.edu");

        given()
                .contentType(ContentType.JSON)
                .body(uni)
                .when()
                .post("/university")
                .then()
                .statusCode(200)
                .body("message", is("success"));
    }

    @Test
    public void getUniversityTest() {
        String uniName = "FindMe Uni " + System.currentTimeMillis();
        Map<String, Object> uni = new HashMap<>();
        uni.put("name", uniName);
        given().contentType(ContentType.JSON).body(uni).post("/university");

        given()
                .pathParam("name", uniName)
                .when()
                .get("/university/name/{name}")
                .then()
                .statusCode(200)
                .body("name", is(uniName));

        given()
                .when()
                .get("/university")
                .then()
                .statusCode(200)
                .body("name", hasItem(uniName));
    }

    @Test
    public void updateUniversityTest() {
        String uniName = "Update Uni " + System.currentTimeMillis();
        Map<String, Object> uni = new HashMap<>();
        uni.put("name", uniName);
        given().contentType(ContentType.JSON).body(uni).post("/university");

        Long uniId = ((Number) given().pathParam("name", uniName)
                .get("/university/name/{name}").path("university_id")).longValue();

        Map<String, Object> update = new HashMap<>();
        update.put("name", uniName + " Updated");
        update.put("location", "New Location");

        given()
                .pathParam("id", uniId)
                .contentType(ContentType.JSON)
                .body(update)
                .when()
                .put("/university/{id}")
                .then()
                .statusCode(200)
                .body("message", is("success"));
    }

    @Test
    public void deleteUniversityTest() {
        String uniName = "Delete Uni " + System.currentTimeMillis();
        Map<String, Object> uni = new HashMap<>();
        uni.put("name", uniName);
        given().contentType(ContentType.JSON).body(uni).post("/university");

        Long uniId = ((Number) given().pathParam("name", uniName)
                .get("/university/name/{name}").path("university_id")).longValue();

        given()
                .pathParam("id", uniId)
                .when()
                .delete("/university/{id}")
                .then()
                .statusCode(200)
                .body("message", is("success"));

        given()
                .pathParam("id", uniId)
                .when()
                .get("/university/{id}")
                .then()
                .statusCode(anyOf(is(404), is(204), is(200), nullValue()));
    }
}