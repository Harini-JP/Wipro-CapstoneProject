package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

public class SearchPage {
    private final WebDriver driver;

    @FindBy(css = ".shelf-item__title, .product-title, .title")
    private List<WebElement> productTitles;

    @FindBy(css = ".sort select, select.sort")
    private WebElement sortDropdown;

    public SearchPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public List<WebElement> getProductTitles() {
        return productTitles;
    }

    public void applyVendorFilter(String vendor) {
        WebElement label = driver.findElement(By.xpath(
            "//label[.//span[normalize-space()='" + vendor + "'] or normalize-space(.)='" + vendor + "']"
        ));
        label.click();
    }

    public void selectSortOrder(String sortOrder) {
        try {
            new Select(sortDropdown).selectByVisibleText(sortOrder);
        } catch (Exception e) {
            for (WebElement opt : sortDropdown.findElements(By.tagName("option"))) {
                if (sortOrder.equalsIgnoreCase(opt.getText().trim())) {
                    opt.click();
                    break;
                }
            }
        }
    }

    public boolean verifyVendorOnly(String vendor) {
        try {
            WebElement label = driver.findElement(By.xpath(
                "//label[.//span[normalize-space()='" + vendor + "'] or normalize-space(.)='" + vendor + "']"
            ));
            WebElement input = label.findElement(By.cssSelector("input"));
            return input.isSelected() || label.getAttribute("class").toLowerCase().contains("active");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean verifySortingByControl(String sortOrder) {
        try {
            Select s = new Select(sortDropdown);
            return sortOrder.equalsIgnoreCase(s.getFirstSelectedOption().getText().trim());
        } catch (Exception e) {
            return sortDropdown.getText().toLowerCase().contains(sortOrder.toLowerCase());
        }
    }
}
