package tests.commonMasters;

import builders.commonMasters.CommonMasterRequestBuilder;
import com.jayway.restassured.response.Response;
import entities.requests.commonMasters.CommonMasterRequest;
import entities.responses.commonMaster.category.CategoryResponse;
import entities.responses.login.LoginResponse;
import org.junit.Assert;
import org.testng.annotations.Test;
import resources.CommonMasterResource;
import tests.BaseAPITest;
import utils.APILogger;
import utils.Properties;
import utils.RequestHelper;
import utils.ResponseHelper;

import java.io.IOException;

public class CategoryTest extends BaseAPITest {

    @Test
    public void categoryTest() throws IOException {

        // Login Test
        LoginResponse loginResponse = loginTestMethod(Properties.devServerUrl, "narasappa");

        // Search Department Test
        categoryTestMethod(loginResponse);
    }

    private void categoryTestMethod(LoginResponse loginResponse) throws IOException {
        CommonMasterRequest commonMasterRequest = new CommonMasterRequestBuilder().build();

        String jsonString = RequestHelper.getJsonString(commonMasterRequest);

        Response response = new CommonMasterResource().searchCategoryTest(jsonString, loginResponse.getAccess_token());

        CategoryResponse categoryResponse = (CategoryResponse)
                ResponseHelper.getResponseAsObject(response.asString(), CategoryResponse.class);

        Assert.assertEquals(categoryResponse.getCategory().length, 3);
        Assert.assertEquals(response.getStatusCode(), 200);

        new APILogger().log("Search Category Test is Completed --");
    }

}
