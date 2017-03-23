package resources;

import com.jayway.restassured.response.Response;
import utils.APILogger;
import utils.Properties;

import static com.jayway.restassured.RestAssured.given;

public class AssetCategoryResource {

    public Response create(String json, String auth_token) {

        new APILogger().log("Create Asset Category Test is started --");
        Response response = given().request().with()
                .header("Content-Type", "application/json")
                .header("auth-token", auth_token)
                .body(json)
                .when()
                .post(Properties.devServerUrl + Properties.assetCategoryCreateUrl);

        return response;
    }

    public Response search(String jsonString, String auth_token) {

        new APILogger().log("Search Asset Category Test is started --");
        Response response = given().request().with()
                .header("Content-Type", "application/json")
                .header("auth-token", auth_token)
                .body(jsonString)
                .when()
                .post(Properties.devServerUrl + Properties.assetCategorySearchUrl);

        return response;
    }
}
