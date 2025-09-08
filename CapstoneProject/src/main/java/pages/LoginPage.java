package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class LoginPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By signInBtn = By.id("signin");
    private final By usernameControl = By.cssSelector("#username div.css-yk16xz-control");
    private final By passwordControl = By.cssSelector("#password div.css-yk16xz-control");
    private final By loginBtn = By.id("login-btn");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(6));
    }
 // ----------------- LoginPage addition -----------------
    /**
     * Return HTML inside the login modal if present; empty string otherwise.
     * Helps debugging and matches tests that call getLoginModalHtml().
     */
    public String getLoginModalHtml() {
        try {
            // Try several selectors that could correspond to a login modal
            By[] modalSelectors = new By[] {
                By.cssSelector("div.login-modal"),
                By.cssSelector("#loginModal"),
                By.cssSelector(".modal.login"),
                By.cssSelector(".auth-modal"),
                By.cssSelector("div.modal-content")
            };

            for (By sel : modalSelectors) {
                List<WebElement> els = driver.findElements(sel);
                if (!els.isEmpty()) {
                    return els.get(0).getAttribute("innerHTML");
                }
            }

            // If nothing matched, attempt to find a login form
            List<WebElement> forms = driver.findElements(By.cssSelector("form#login, form.login-form, form[name='login']"));
            if (!forms.isEmpty()) {
                return forms.get(0).getAttribute("innerHTML");
            }

            return "";
        } catch (Exception e) {
            // In debug mode we prefer to return empty string than throw
            System.err.println("DEBUG: getLoginModalHtml() error: " + e.getMessage());
            return "";
        }
    }


 // LoginPage.java
    public void openLoginForm() {
        // Click the "Sign In" button to open modal
        WebElement signInBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("signin"))); // safer than css chain
        signInBtn.click();

        // Wait for the login modal to appear
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#login-btn, div#username")));

        // If dropdown not yet visible, click username field
        List<WebElement> dropdowns = driver.findElements(By.cssSelector("#username div.css-yk16xz-control"));
        if (dropdowns.isEmpty()) {
           
            WebElement usernameField = driver.findElement(By.id("username"));
            usernameField.click();
        }

        // Finally wait for the dropdown control to appear
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#username div.css-yk16xz-control")));
        System.out.println("DEBUG: Login form opened successfully ✅");
    }


    private void selectCustomDropdown(String containerId, String visibleText) {
        By control = By.cssSelector("#" + containerId + " div.css-yk16xz-control");
        wait.until(ExpectedConditions.elementToBeClickable(control));

        // If already selected and matches, return
        try {
            WebElement current = driver.findElement(By.cssSelector("#" + containerId + " div.css-1uccc91-singleValue"));
            if (current != null && visibleText != null &&
                current.getText().trim().equalsIgnoreCase(visibleText.trim())) {
                return;
            }
        } catch (Exception ignored) { /* not selected yet */ }

        // open dropdown and click first matching option 
        driver.findElement(control).click();
        By optionXpath = By.xpath("//div[@id='" + containerId + "']//div[contains(normalize-space(.), \"" + visibleText + "\")]");
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(optionXpath));
        List<WebElement> opts = driver.findElements(optionXpath);
        if (opts.isEmpty()) {
            throw new RuntimeException("Option containing '" + visibleText + "' not found for: " + containerId);
        }
        opts.get(0).click();

        // small pause to allow UI to update 
        try { Thread.sleep(200); } catch (InterruptedException ignored) {}
    }

    public void selectUsername(String username) {
        selectCustomDropdown("username", username);
    }

    public void selectPassword(String password) {
        selectCustomDropdown("password", password);
    }

    public void clickLogin() {
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();
    }

    public String getPageTitle() {
        return driver.getTitle();
    }
}
