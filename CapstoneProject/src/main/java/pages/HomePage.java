package pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class HomePage {
    private final WebDriver driver;

    @FindBy(css = "input[placeholder='Search']")
    private WebElement searchBox;

    @FindBy(xpath = "//button[contains(text(),'Search')]")
    private WebElement searchButton;

    public HomePage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public WebElement getSearchBox() {
        return searchBox;
    }

    public void enterText(String text) {
        searchBox.clear();
        searchBox.sendKeys(text);
    }

    public void clickSearch() {
        searchButton.click();
    }

    public String getSearchBoxText() {
        return searchBox.getAttribute("value");
    }
}
