package stepDefinitions;

import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;

import pages.LoginPage;
import pages.ProductPage;
import pages.CartPage;
import pages.CheckoutPage;
import runners.TestRunner;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class EndToEndSteps {
    WebDriver driver = TestRunner.driver;
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(12));

    LoginPage loginPage = new LoginPage(driver);
    ProductPage productPage = new ProductPage(driver);
    CartPage cartPage = new CartPage(driver);
    CheckoutPage checkoutPage = new CheckoutPage(driver);

    @Given("user launches browser")
    public void user_launches_browser() {
        driver.get("https://bstackdemo.com/");
        driver.manage().window().maximize();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.shelf-item")));
    }

    @And("user logs in with {string} and {string}")
    public void user_logs_in_with_and(String username, String password) {
        loginPage.openLoginForm();
        loginPage.selectUsername(username);
        loginPage.selectPassword(password);
        loginPage.clickLogin();

        // ✅ More robust post-login wait
        wait.until(d ->
            d.findElements(By.xpath("//*[contains(text(),'" + username + "')]")).size() > 0
            || d.findElements(By.cssSelector("#logout, .logout-link, a.logout")).size() > 0
        );
    }


    @When("user adds {string} to the cart")
    public void user_adds_to_the_cart(String productName) {
        boolean added = productPage.addProductToCartByName(productName);
        Assert.assertTrue(added, "Failed to add product: " + productName);

        // Wait until cart has at least 1 item
        wait.until(d -> d.findElements(By.cssSelector("div.float-cart__shelf-container .shelf-item")).size() > 0);
        System.out.println("DEBUG: Product added to cart: " + productName);
    }
    @When("user adds {string} again to the cart")
    public void user_adds_again_to_the_cart(String productName) {
        boolean added = productPage.addProductToCartByName(productName);
        Assert.assertTrue(added, "Failed to add product: " + productName);

        // Wait until cart has at least 1 item (reuse your existing wait logic)
        wait.until(d -> d.findElements(By.cssSelector("div.float-cart__shelf-container .shelf-item")).size() > 0);
        System.out.println("DEBUG: Product added to cart (again): " + productName);
    }



    @And("user removes the product from the cart")
    public void user_removes_the_product_from_the_cart() {
        By cartItems = By.cssSelector("div.float-cart__shelf-container .shelf-item");
        By removeBtn = By.cssSelector("div.float-cart__shelf-container .shelf-item .shelf-item__del");

        // Ensure cart has at least one item
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(cartItems, 0));

        // Click remove button (use JS if normal click fails)
        WebElement remove = driver.findElement(removeBtn);
        try {
            remove.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", remove);
        }

        // Now wait until cart becomes empty
        boolean cartEmpty = wait.until(driver -> 
            driver.findElements(cartItems).isEmpty()
        );

        Assert.assertTrue(cartEmpty, "Cart is not empty after removing product");
        System.out.println("DEBUG: Cart is empty after removing the product ✅");
    }




    @And("user proceeds to checkout")
    public void user_proceeds_to_checkout() {
        driver.get("https://bstackdemo.com/checkout");
        checkoutPage.waitForForm();
    }

    @And("user fills shipping details")
    public void user_fills_shipping_details() {
        checkoutPage.fillShippingForm("Hari", "J", "123 Demo St", "CA", "90001");
        checkoutPage.submitShipping();
    }

    @Then("user should reach the confirmation page")
    public void user_should_reach_the_confirmation_page() {
        boolean reached = wait.until(d -> d.getCurrentUrl().contains("/confirmation") || d.findElements(By.id("downloadpdf")).size() > 0);
        if (!reached) throw new RuntimeException("Did not reach confirmation page. Current URL: " + driver.getCurrentUrl());
    }

    @And("user logs out successfully")
    public void user_logs_out_successfully() throws InterruptedException {
        // candidate locators for logout
        List<By> logoutLocators = Arrays.asList(
            By.xpath("//*[text()='Logout' or text()='logout' or normalize-space(.)='Logout' or normalize-space(.)='logout']"),
            By.id("logout"),
            By.cssSelector("a.logout, button.logout, .logout-link, .btn-logout"),
            By.xpath("//a[contains(@href,'logout') or contains(@onclick,'logout')]")
        );

        // candidate locators to open profile/menu if logout is hidden inside a dropdown
        List<By> menuLocators = Arrays.asList(
            By.cssSelector(".nav-user, .user-menu, .dropdown-toggle, .profile-menu"),
            By.xpath("//*[contains(@class,'user') and (contains(.,'Account') or contains(.,'Profile'))]")
        );

        boolean clicked = false;

        // try direct logout locators first
        for (By loc : logoutLocators) {
            List<WebElement> els = driver.findElements(loc);
            if (!els.isEmpty()) {
                WebElement logoutBtn = els.get(0);
                try {
                    logoutBtn.click();
                } catch (Exception e) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", logoutBtn);
                }
                clicked = true;
                break;
            }
        }

        // if we didn't find logout directly, try opening menus then look again
        if (!clicked) {
            for (By menuLoc : menuLocators) {
                List<WebElement> menus = driver.findElements(menuLoc);
                if (!menus.isEmpty()) {
                    try {
                        menus.get(0).click();
                    } catch (Exception e) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", menus.get(0));
                    }
                    // short wait for menu contents to appear
                    try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                    // attempt logout locators again
                    for (By loc : logoutLocators) {
                        List<WebElement> els = driver.findElements(loc);
                        if (!els.isEmpty()) {
                            try {
                                els.get(0).click();
                            } catch (Exception e) {
                                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", els.get(0));
                            }
                            clicked = true;
                            break;
                        }
                    }
                    if (clicked) break;
                }
            }
        }

        // final fallback: navigate to home page and attempt logout (some apps show logout on homepage)
        if (!clicked) {
            driver.get("https://bstackdemo.com/");
            // small wait for page load
            try { Thread.sleep(800); } catch (InterruptedException ignored) {}
            for (By loc : logoutLocators) {
                List<WebElement> els = driver.findElements(loc);
                if (!els.isEmpty()) {
                    try {
                        els.get(0).click();
                    } catch (Exception e) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", els.get(0));
                    }
                    clicked = true;
                    break;
                }
            }
        }

        // wait for signin element as proof of successful logout
        try {
            wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.id("signin")),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("a#signin, #login, .signin, input[value='Sign in']"))
            ));
        } catch (Exception e) {
            // if logout wasn't clicked or signin didn't appear, fail with helpful message
            if (!clicked) {
                Assert.fail("Could not find or click a Logout element. Tried locators and menu opens. Current page title: "
                        + driver.getTitle() + " URL: " + driver.getCurrentUrl());
            } else {
                Assert.fail("Logout was clicked but Signin element did not appear within the wait timeout. Current URL: "
                        + driver.getCurrentUrl());
            }
        }
    }
}
