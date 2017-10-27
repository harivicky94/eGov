package pages;

import entities.employeeManagement.createEmployee.EmployeeDetails;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

public class DashboardPage extends BasePage {
    private WebDriver driver;

    @FindBy(id = "searchtree")
    private WebElement searchTreeTextBox;

    @FindBy(css = ".list a")
    private List<WebElement> searchResults;

    @FindBy(className = "profile-name")
    private WebElement profileNameLink;

    @FindBy(linkText = "Sign Out")
    private WebElement signOutLink;

    @FindBy(linkText = "Cheque Assignment")
    private List<WebElement> chequeAssignment;

    @FindBy(linkText = "RTGS Assignment")
    private WebElement rtgsAssignment;

    @FindBy(css = "li[class='dropdown'] a[data-work='worklist']")
    private WebElement officialInboxTable;

    @FindBy(css = "li[class='dropdown'] a[data-work='drafts']")
    private WebElement officialDraftsTable;

    @FindBy(id = "inboxsearch")
    private WebElement inboxsearch;

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
    }

    private void searchFor(String value) {
        enterText(searchTreeTextBox, value, driver);
    }

    public void logOut() {
        clickOnButton(profileNameLink, driver);
        jsClick(signOutLink, driver);
    }

    public void chooseForModeOFAssignment(String mode) {

        if (mode.equalsIgnoreCase("cheque")) {
            searchFor("Cheque Assignment");
            waitForElementToBeClickable(chequeAssignment.get(0), driver);
            chequeAssignment.get(0).click();
            switchToNewlyOpenedWindow(driver);
        } else {
            searchFor("RTGS Assignment");
            clickOnButton(rtgsAssignment, driver);
            switchToNewlyOpenedWindow(driver);
        }
    }

    public void chooseScreen(String screenName) {
        searchFor(screenName);
        waitForElementToBePresent(By.cssSelector(".list a"), driver);
        searchResults.stream().filter(searchResult -> searchResult.getText().equalsIgnoreCase(screenName)).findFirst().get().click();
        switchToNewlyOpenedWindow(driver);
    }

    public void chooseScreen(String screenName, String condition) {
        searchFor(screenName);
        waitForElementToBePresent(By.cssSelector(".list a"), driver);
        Optional<WebElement> href = searchResults.stream().filter(searchResult -> {
            return searchResult.getText().equalsIgnoreCase(screenName) && searchResult.getAttribute("href").contains(condition);
        }).findFirst();
        if (href.isPresent()) {
            href.get().click();
        }
        switchToNewlyOpenedWindow(driver);
    }

    public void openApplication(String number) {
        driver.navigate().refresh();
        WebElement element = getApplicationRow(number);
        try {
            clickOnButton(element, driver);
        } catch (Exception e) {
            jsClick(element, driver);
        }
        switchToNewlyOpenedWindow(driver);
    }

    private WebElement getApplicationRow(String number) {
        List<WebElement> totalRows;
        try {
            await().atMost(130, SECONDS).until(() -> driver.findElements(By.cssSelector("[id='official_inbox'] tr td")).size() > 1);
            totalRows = driver.findElement(By.id("official_inbox")).findElement(By.tagName("tbody")).findElements(By.tagName("tr"));
            for (WebElement applicationRow : totalRows) {
                if (applicationRow.findElements(By.tagName("td")).get(4).getText().contains(number)) {
                    return applicationRow;
                }
            }
            throw new RuntimeException("No application row found in Inbox -- " + number);
        } catch (Exception e) {
            clickOnButton(officialDraftsTable, driver);
            await().atMost(130, SECONDS).until(() -> driver.findElements(By.cssSelector("[id='official_drafts'] tr td")).size() > 1);
            totalRows = driver.findElement(By.id("official_drafts")).findElement(By.tagName("tbody")).findElements(By.tagName("tr"));
            for (WebElement applicationRow : totalRows) {
                if (applicationRow.findElements(By.tagName("td")).get(4).getText().contains(number))
                    return applicationRow;
            }
            throw new RuntimeException("No application row found in Inbox and Drafts -- " + number);
        }
    }

    public void verifyApplication(String applicationNumber) {
        driver.navigate().refresh();
        enterText(inboxsearch, applicationNumber, driver);
        String actMsg = driver.findElement(By.xpath(".//*[@id='official_inbox']/tbody/tr/td")).getText();
        Arrays.asList(actMsg.split("\\ ")).contains("No");
    }

    public void enterPasswordResetDetails(EmployeeDetails employee) {
        enterText(driver.findElement(By.id("username")), employee.getUserName(), driver);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        clickOnButton(driver.findElement(By.className("tt-dropdown-menu")), driver);
        enterText(driver.findElement(By.cssSelector("[name='password']")), "kurnool_eGov@123", driver);
        clickOnButton(driver.findElement(By.id("submitbtn")), driver);
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        clickOnButton(driver.findElement(By.cssSelector(".btn.btn-default")), driver);
        switchToPreviouslyOpenedWindow(driver);
    }
}