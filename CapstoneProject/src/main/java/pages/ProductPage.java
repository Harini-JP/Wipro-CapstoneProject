// pages/ProductPage.java
package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import java.util.List;

public class ProductPage {
    private WebDriver driver;

    // Locators
    private By productContainer = By.cssSelector("div.shelf-item");
    private By productName = By.cssSelector("p.shelf-item__title");
    private By addToCartBtn = By.cssSelector("div.shelf-item__buy-btn");

    public ProductPage(WebDriver driver) {
        this.driver = driver;
    }

    // Add first product in list
    public void addFirstProductToCart() {
        List<WebElement> items = driver.findElements(productContainer);
        System.out.println("DEBUG: Found product count = " + items.size());
        if (items.size() > 0) {
            items.get(0).findElement(addToCartBtn).click();
        } else {
            System.err.println("DEBUG: No products found to add.");
        }
    }
    

    // Add product by partial name (contains match)
    public boolean addProductToCartByName(String name) {
        List<WebElement> items = driver.findElements(productContainer);
        System.out.println("DEBUG: Searching " + items.size() + " products for name containing: " + name);
        for (WebElement item : items) {
            try {
                String title = item.findElement(productName).getText().trim();
                if (title.toLowerCase().contains(name.trim().toLowerCase())) {
                    item.findElement(addToCartBtn).click();
                    System.out.println("DEBUG: Clicked add for product: " + title);
                    return true;
                }
            } catch (Exception e) { }
        }
        System.err.println("DEBUG: No product matched name: " + name);
        return false;
    }
    
}
