package tables.claim;

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
public class BrendenlClaimSystemTest {

    @LocalServerPort
    int port;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.registerParser("text/plain", io.restassured.parsing.Parser.JSON);
    }

    @Test
    public void createAndVerifyClaimTest() {
        String username = "ClaimTest1" + System.currentTimeMillis();
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("email", username + "@test.com");
        user.put("pass_hash", "pass");
        user.put("role", "USER");

        long userId = ((Number) given()
                .contentType(ContentType.JSON)
                .body(user)
                .post("/users")
                .path("user_id")).longValue();

        Map<String, Object> claim = new HashMap<>();
        claim.put("type", "ACCOUNT_DELETION");
        claim.put("description", "I need to delete my account");

        Map<String, Object> userRef = new HashMap<>();
        userRef.put("user_id", userId);
        claim.put("user", userRef);

        given()
                .contentType(ContentType.JSON)
                .body(claim)
                .when()
                .post("/claims")
                .then()
                .statusCode(200)
                .extract().asString().contains("success");
        given()
                .pathParam("userId", userId)
                .when()
                .get("/claims/user/{userId}")
                .then()
                .statusCode(200)
                .body("[0].description", is("I need to delete my account"));
    }

    @Test
    public void updateClaimStatusTest() {
        String username = "StatusUser" + System.currentTimeMillis();
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("email", username + "@test.com");
        user.put("pass_hash", "pass");
        user.put("role", "USER");

        long userId = ((Number) given()
                .contentType(ContentType.JSON)
                .body(user)
                .post("/users")
                .path("user_id")).longValue();

        Map<String, Object> claim = new HashMap<>();
        claim.put("type", "OTHER");
        claim.put("description", "Status check");

        Map<String, Object> userRef = new HashMap<>();
        userRef.put("user_id", userId);
        claim.put("user", userRef);

        given()
                .contentType(ContentType.JSON)
                .body(claim)
                .post("/claims")
                .then()
                .statusCode(200);

        long claimId = ((Number) given()
                .pathParam("userId", userId)
                .when()
                .get("/claims/user/{userId}")
                .then()
                .log().ifValidationFails()
                .extract()
                .path("[0].claim_id")).longValue();

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("status", "RESOLVED");

        given()
                .pathParam("id", claimId)
                .contentType(ContentType.JSON)
                .body(updateData)
                .when()
                .put("/claims/{id}")
                .then()
                .statusCode(200)
                .body("message", containsString("success"));
    }

    @Test
    public void getClaimsByStatusTest() {
        given()
                .pathParam("status", "PENDING")
                .when()
                .get("/claims/status/{status}")
                .then()
                .statusCode(200)
                .body("status", everyItem(is("PENDING")));
    }

    @Test
    public void deleteClaimTest() {
        given()
                .pathParam("id", 99999)
                .when()
                .delete("/claims/{id}")
                .then()
                .statusCode(200);
    }
}