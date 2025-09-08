package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

/**
 * Lightweight checkout page helpers.
 */
public class CheckoutPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final By checkoutFormRoot = By.cssSelector("div.checkout-form, div.checkout-view-content, #checkout-app");
    private final By submitButton = By.cssSelector("button#checkout-shipping-continue, button[type=submit].button--primary, button[type=submit]");

    // Direct locator for Address
    private final By addressInput = By.id("addressLine1Input");

    public CheckoutPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(8));
    }

    // Tolerant input locator for other fields
    private By inputForLabel(String labelText) {
        String xpath =
            ".//label[contains(normalize-space(.), '" + labelText + "')]"
            + "/following::input[1] | .//label[contains(normalize-space(.), '" + labelText + "')]"
            + "/following::textarea[1] | .//*[contains(normalize-space(.), '" + labelText + "')]/following::input[1]";
        return By.xpath(xpath);
    }

    // Wait until checkout form is visible
    public void waitForForm() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(checkoutFormRoot));
    }

    public void fillFirstName(String firstName) {
        waitForForm();
        By locator = inputForLabel("First Name");
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).clear();
        driver.findElement(locator).sendKeys(firstName);
    }

    public void fillLastName(String lastName) {
        By locator = inputForLabel("Last Name");
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).clear();
        driver.findElement(locator).sendKeys(lastName);
    }

    public void fillAddress(String addr) {
        waitForForm();
        wait.until(ExpectedConditions.visibilityOfElementLocated(addressInput)).clear();
        driver.findElement(addressInput).sendKeys(addr);
    }

    public void fillProvince(String prov) {
        By locator = inputForLabel("State/Province");
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).clear();
        driver.findElement(locator).sendKeys(prov);
    }

    public void fillPostalCode(String pc) {
        By locator = inputForLabel("Postal Code");
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).clear();
        driver.findElement(locator).sendKeys(pc);
    }

    /** Fill all shipping fields in one call */
    public void fillShippingForm(String fn, String ln, String addr, String prov, String pc) {
        waitForForm();
        fillFirstName(fn);
        fillLastName(ln);
        fillAddress(addr);
        fillProvince(prov);
        fillPostalCode(pc);
    }

    /** Click the continue/submit button on shipping step */
    public void submitShipping() {
        waitForForm();
        wait.until(ExpectedConditions.elementToBeClickable(submitButton)).click();
    }
}
