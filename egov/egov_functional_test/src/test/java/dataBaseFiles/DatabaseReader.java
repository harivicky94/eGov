package dataBaseFiles;

import builders.LoginDetailsBuilder;
import entities.LoginDetails;
import steps.PageStore;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static steps.BaseSteps.pageStore;

public class DatabaseReader extends PageStore {


    public LoginDetails getLoginDetails(String currentUser) throws SQLException {
        String dbquery = "select * from eg_user";
        Statement stmt = pageStore.dbConnection().createStatement();
        ResultSet rs = stmt.executeQuery(dbquery);
        String id = null;
        String password = null;
        while (rs.next())
        {
            if(rs.getString("positions").equalsIgnoreCase(currentUser)) {
                id = rs.getString("userid");
                password = rs.getString("password");
            }
        }
        return new LoginDetailsBuilder().withLoginId(id).withPassword(password)
//                .withHasZone(hasZone)
                .build();
    }
}
