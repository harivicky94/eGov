package steps;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import utils.Properties;
import utils.ScenarioContext;

import java.io.IOException;

public class StartingSteps extends BaseSteps {

    @Before
    public void beforeScenario() throws Exception {
        //Kill All Google Chrome Instances
//        killAllGoogleChromeInstances();
        scenarioContext = new ScenarioContext();
        pageStore = new PageStore();
        pageStore.getDriver().get(Properties.url);
        pageStore.getDriver().manage().window().maximize();
    }

    @After
    public void afterScenario(Scenario scenario) {
        if (scenario.isFailed()) {
            final byte[] screenshot = ((TakesScreenshot) pageStore.getDriver())
                    .getScreenshotAs(OutputType.BYTES);
            scenario.embed(screenshot, "image/png");
        }
        pageStore.pages.clear();
        pageStore.webDriver.manage().deleteAllCookies();
        pageStore.destroy();
    }

    private void killAllGoogleChromeInstances() throws Exception {
        String killAllGoogleChromeLinux = "killall chrome";
        String killAllGoogleChromeMac = "killall chromedriver";
        String killAllGoogleChromeStable = "killall google-chrome-stable";
        String killAllGoogleChromeInJenkins = "killall google-chrome-stable";

        Process p = Runtime.getRuntime().exec(killAllGoogleChromeLinux);
        p.waitFor();

        Process p1 = Runtime.getRuntime().exec(killAllGoogleChromeMac);
        p1.waitFor();

        Process p2 = Runtime.getRuntime().exec(killAllGoogleChromeStable);
        p2.waitFor();

        Process p3 = Runtime.getRuntime().exec(killAllGoogleChromeInJenkins);
        p3.waitFor();
    }
}