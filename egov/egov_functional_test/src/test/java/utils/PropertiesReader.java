package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesReader {
    private Properties prop = new Properties();
    InputStream input = null;
    String env = System.getProperty("env");

    public PropertiesReader() {
        try {

            System.out.println("ENV: " + env);
//
//            if (null == env) env = "local";

//            String propertiesFilePath = "/gradle.properties";
//            InputStream inputStream;
//            inputStream = getInputStream(propertiesFilePath);
//            prop.load(inputStream);
            input = new FileInputStream("gradle.properties");

            // load a properties file
            prop.load(input);

            // get the property value and print it out
//            System.out.println(prop.getProperty("dbuser"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private InputStream getInputStream(String propertiesFilePath) {
//        return this.getClass().getClassLoader().getResourceAsStream(propertiesFilePath);
//    }

    public String getUrl() {
        System.out.println("URL = "+prop.getProperty(env+".url"));
        return prop.getProperty(env+".url");
    }

    public String getDburl(){return prop.getProperty(env+".dburl");}

    public String getDbuser(){return prop.getProperty(env+".dbuser");}

    public String getDbpassword(){return prop.getProperty(env+".dbpassword");}

}

