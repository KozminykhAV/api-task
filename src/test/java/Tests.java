import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class Tests {

    @BeforeMethod
    void before() {
        Specifications.setRequest(Specifications.requestSpec());
    }

    @Test(testName = "1.1 - Получение списка пользователей", priority = 1)
    void getList() {
        Specifications.setResponse(Specifications.responseSpec(200));
        List<LinkedHashMap<String, Object>> userList = given()
                .when()
                .get("/api/users?page=2")
                .then()
                .extract().response()
                .getBody().jsonPath().getList("data");
        userList.forEach(map -> {
            map.forEach((key, value) -> System.out.println(key + ": " + value));
            System.out.println();
        });
    }

    @Test(testName = "1.2 - Проверка что имена файлов аватаров пользователей совпадают", priority = 2)
    void checkNames() {
        Specifications.setResponse(Specifications.responseSpec(200));
        List<String> avatarLinks = given()
                .when()
                .get("/api/users?page=2")
                .then()
                .extract().response()
                .getBody().jsonPath().getList("data.avatar");
        List<String> fileNames = new ArrayList<>();
        avatarLinks.forEach(u -> fileNames.add(u.substring(u.lastIndexOf('/') + 1, u.lastIndexOf('.'))));
        String sample = fileNames.get(0);
        fileNames.forEach(name -> Assert.assertEquals(name, sample));
    }

    @Test(testName = "2.1 - Тестирование успешной регистрации", priority = 3)
    void successful() {
        Specifications.setResponse(Specifications.responseSpec(200));
        Map<String, String> user = Map.of(
                "email", "eve.holt@reqres.in",
                "password", "pistol"
        );
        Response r = given()
                .body(user)
                .when()
                .post("/api/register")
                .then()
                .log().all()
                .extract().response();
        Assert.assertEquals(r.jsonPath().get("id").toString(), "4");
        Assert.assertEquals(r.jsonPath().get("token"), "QpwL5tke4Pnpja7X4");
    }

    @Test(testName = "2.2 - Тестирование ошибки регистрации из-за отсутствия пароля", priority = 4)
    void unsuccessful() {
        Specifications.setResponse(Specifications.responseSpec(400));
        Map<String, String> user = Map.of("email", "sydney@fife");
        Response r = given()
                .body(user)
                .when()
                .post("/api/register")
                .then()
                .log().all()
                .extract().response();
        Assert.assertEquals(r.jsonPath().get("error"), "Missing password");
    }

    @Test(testName = "3 - Проверка что LIST <RESOURCE> возвращает данные отсортированные по годам", priority = 5)
    void checkOperation() {
        Specifications.setResponse(Specifications.responseSpec(200));
        List<Integer> data = given()
                .when()
                .get("api/unknown")
                .then()
                .log().all()
                .extract().response()
                .getBody().jsonPath().getList("data.year");
        for (int i = 0; i < data.size() - 1; i++) {
            Assert.assertTrue(data.get(i) < data.get(i + 1));
        }
    }
}
