package steps;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import utils.Properties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PageStore {

    WebDriver webDriver;
    List<Object> pages;
    Connection conn = null;

    public PageStore() {
        webDriver = new LocalDriver().getApplicationDriver();
        pages = new ArrayList<Object>();
    }

    private boolean runningOnLocal() {
        return System.getProperty("driver").equals("local");
    }


    public <T> T get(Class<T> clazz) {
        for (Object page : pages) {
            if (page.getClass() == clazz)
                return (T) page;
        }
        T page = PageFactory.initElements(webDriver, clazz);
        pages.add(page);
        return page;
    }


    public void destroy() {
        webDriver.quit();
    }

    public WebDriver getDriver() {
        webDriver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        return webDriver;
    }

    public Connection dbConnection() {
        try {
            if (conn == null || (conn != null && conn.isClosed())) {
                Class.forName("org.postgresql.Driver");
                conn = DriverManager.getConnection(Properties.dburl, Properties.dbuser, Properties.dbpassword);
                System.out.println("Connected to Database successfully");
                System.out.println("DBURL = "+Properties.dburl);
                System.out.println("DBUser = "+Properties.dbuser);
                System.out.println("DBPassword = "+Properties.dbpassword);
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return conn;
    }

}
