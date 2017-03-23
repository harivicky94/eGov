package tests.commonMasters;

import builders.commonMasters.CommonMasterRequestBuilder;
import com.jayway.restassured.response.Response;
import entities.requests.commonMasters.CommonMasterRequest;
import entities.responses.commonMaster.department.DepartmentResponse;
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

public class DepartmentTest extends BaseAPITest {

    @Test
    public void departmentTest() throws IOException {

        // Login Test
        LoginResponse loginResponse = loginTestMethod(Properties.devServerUrl, "narasappa");

        // Search Department Test
        departmentTestMethod(loginResponse);
    }

    private void departmentTestMethod(LoginResponse loginResponse) throws IOException {
        CommonMasterRequest commonMasterRequest = new CommonMasterRequestBuilder().build();

        String jsonString = RequestHelper.getJsonString(commonMasterRequest);

        Response response = new CommonMasterResource().searchDepartmentTest(jsonString, loginResponse.getAccess_token());

        DepartmentResponse departmentResponse = (DepartmentResponse)
                ResponseHelper.getResponseAsObject(response.asString(), DepartmentResponse.class);

        Assert.assertEquals(departmentResponse.getDepartment().length, 10);
        Assert.assertEquals(response.getStatusCode(), 200);

        new APILogger().log("Search Department Test is Completed --");
    }
}
