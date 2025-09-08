package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class SignUpPage {
    private WebDriver driver;

    // Mock locators (adjust if real app has signup page)
    private By signupLink = By.id("signup-link");
    private By usernameField = By.id("newUsername");
    private By emailField = By.id("email");
    private By passwordField = By.id("newPassword");
    private By signupBtn = By.id("signup-btn");

    public SignUpPage(WebDriver driver) {
        this.driver = driver;
    }

    public void openSignupPage() {
        driver.findElement(signupLink).click();
    }

    public void enterUsername(String username) {
        driver.findElement(usernameField).sendKeys(username);
    }

    public void enterEmail(String email) {
        driver.findElement(emailField).sendKeys(email);
    }

    public void enterPassword(String password) {
        driver.findElement(passwordField).sendKeys(password);
    }

    public void clickSignup() {
        driver.findElement(signupBtn).click();
    }
}
