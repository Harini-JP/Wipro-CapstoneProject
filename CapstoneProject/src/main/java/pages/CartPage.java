// pages/CartPage.java
package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class CartPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    
    private final By cartRoot = By.cssSelector("div.float-cart");
    private final By cartItems = By.cssSelector("div.float-cart .shelf-item");
    private final By itemNameInCart = By.cssSelector(".shelf-item__details > p, .shelf-item__title, .shelf-item__details");
    private final By subtotalSel = By.cssSelector("p.sub-price__val, .sub-price__val, .cart-subtotal, .subtotal");

    public CartPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(8));
    }
 // place inside your CartPage class
    public boolean isCartOpen() {
        try {
            waitUntilCartVisible(); // existing helper in CartPage
            List<WebElement> items = driver.findElements(cartItems);
            return items != null && !items.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }


    // Wait until cart root is visible (floating cart open)
    public void waitUntilCartVisible() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(cartRoot));
        } catch (Exception e) {
           
            System.err.println("DEBUG: waitUntilCartVisible timed out: " + e.getMessage());
            throw e;
        }
    }

    // Wait until at least one cart item exists 
    public void waitForCartItems() {
        try {
            wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(cartItems, 0));
        } catch (Exception e) {
            System.err.println("DEBUG: waitForCartItems timed out: " + e.getMessage());
            
        }
    }

    // Return visible item names in cart
    public List<String> getCartItemNames() {
        List<String> names = new ArrayList<>();
        waitUntilCartVisible();
        List<WebElement> items = driver.findElements(cartItems);
        for (WebElement item : items) {
            try {
                WebElement nameEl = item.findElement(itemNameInCart);
                names.add(nameEl.getText().trim());
            } catch (Exception e) {
                names.add(item.getText().trim());
            }
        }
        return names;
    }

    public boolean isItemPresent(String partialName) {
        try {
            waitForCartItems();
        } catch (Exception ignored) {}
        return getCartItemNames().stream()
                .anyMatch(n -> n.toLowerCase().contains(partialName.toLowerCase()));
    }

  
    public int getQuantityForItem(String partialName) {
        try {
            waitUntilCartVisible();
        } catch (Exception ignored) {}
        List<WebElement> items = driver.findElements(cartItems);
        for (WebElement it : items) {
            String title = "";
            try { title = it.findElement(itemNameInCart).getText().trim(); } catch (Exception e) { title = it.getText(); }
            if (!title.toLowerCase().contains(partialName.toLowerCase())) continue;

            // 1) look specifically for "Quantity" label and digits after it
            try {
                String whole = it.getText();
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("Quantity[:\\s]*([0-9]+)", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(whole);
                if (m.find()) {
                    int val = Integer.parseInt(m.group(1));
                    System.out.println("DEBUG: Parsed qty for '" + title + "' via labeled Quantity regex = " + val);
                    return val;
                }
            } catch (Exception ignored) {}

            // 2) try common qty selectors inside this item
            By[] qSelectors = new By[] {
                By.cssSelector(".shelf-item__quantity"),
                By.cssSelector(".quantity"),
                By.cssSelector(".qty"),
                By.cssSelector(".shelf-item__details small")
            };
            for (By qSel : qSelectors) {
                try {
                    List<WebElement> qEls = it.findElements(qSel);
                    if (!qEls.isEmpty()) {
                        for (WebElement q : qEls) {
                            String digits = q.getText().replaceAll("[^0-9]", "");
                            if (!digits.isEmpty()) {
                                int val = Integer.parseInt(digits);
                                System.out.println("DEBUG: Parsed qty for '" + title + "' using " + qSel + " = " + val);
                                return val;
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }

            // 3) fallback: parse any number in the item text 
            try {
                String all = it.getText();
                java.util.regex.Matcher m2 = java.util.regex.Pattern.compile("\\b([0-9]+)\\b").matcher(all);
                if (m2.find()) {
                    int val = Integer.parseInt(m2.group(1));
                    System.out.println("DEBUG: Parsed qty for '" + title + "' via loose number = " + val);
                    return val;
                }
            } catch (Exception ignored) {}

            System.err.println("DEBUG: Could not parse quantity for item: " + title);
            return -1;
        }
        System.err.println("DEBUG: Item with name containing '" + partialName + "' not found in cart.");
        return -1;
    }


    /**
     * Increase quantity for the matching item by clicking its + button `times` times.
     * Returns true if clicks were attempted; waits for a UI change (qty or subtotal) when possible.
     */
    public boolean increaseQtyForItem(String partialName, int times) {
        try {
            waitUntilCartVisible();
        } catch (Exception ignored) {}

        List<WebElement> items = driver.findElements(cartItems);
        for (WebElement it : items) {
            String title = "";
            try { title = it.findElement(itemNameInCart).getText().trim(); } catch (Exception e) { title = it.getText(); }
            if (!title.toLowerCase().contains(partialName.toLowerCase())) continue;

            // find candidate plus buttons inside this item
            List<WebElement> changeBtns = it.findElements(By.cssSelector("button.change-product-button, .change-product-button, button"));
            WebElement plus = null;
            for (WebElement b : changeBtns) {
                try {
                    String txt = b.getText().trim();
                    if (txt.contains("+")) { plus = b; break; }
                } catch (Exception ignored) {}
            }
            if (plus == null && !changeBtns.isEmpty()) plus = changeBtns.get(changeBtns.size()-1);

            if (plus == null) {
                System.err.println("DEBUG: no plus button found inside item: " + title);
                return false;
            }

            int beforeQty = getQuantityForItem(partialName);
            String beforeSub = getSubtotal();

            for (int i = 0; i < times; i++) {
                try {
                    wait.until(ExpectedConditions.elementToBeClickable(plus)).click();
                } catch (Exception e) {
                    // fallback to JS click
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", plus);
                }
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}
            }

            // Wait for either qty increase or subtotal change (5s)
            try {
                new WebDriverWait(driver, Duration.ofSeconds(5)).until(d -> {
                    int nowQty = getQuantityForItem(partialName);
                    String nowSub = getSubtotal();
                    if (beforeQty == -1 && nowQty == -1) {
                        // if qty not shown, accept subtotal change as indicator
                        return nowSub != null && !nowSub.equals(beforeSub) && !nowSub.isEmpty();
                    }
                    return nowQty > beforeQty;
                });
            } catch (Exception e) {
                System.err.println("DEBUG: quantity/subtotal did not change within timeout for: " + partialName);
                // still return true because clicks were performed
                return true;
            }

            System.out.println("DEBUG: increaseQtyForItem - succeeded for: " + partialName);
            return true;
        }
        System.err.println("DEBUG: item not found to increase: " + partialName);
        return false;
    }

    public String getSubtotal() {
        try {
            waitUntilCartVisible();
        } catch (Exception ignored) {}
        try {
            List<WebElement> els = driver.findElements(subtotalSel);
            if (!els.isEmpty()) {
                return els.get(0).getText().trim();
            } else {
                // fallback: try any element containing subtotal keywords
                List<WebElement> cand = driver.findElements(By.xpath("//*[contains(text(),'SUBTOTAL') or contains(text(),'Subtotal') or contains(text(),'total')]"));
                if (!cand.isEmpty()) return cand.get(0).getText().trim();
            }
        } catch (Exception e) {
            System.err.println("DEBUG: getSubtotal failed: " + e.getMessage());
        }
        return "";
    }

    /**
     * Remove first matching item by partial name and wait until it disappears.
     */
    /**
     * Remove first matching item by partial name and wait until it disappears.
     */
    public boolean removeItemByName(String partialName) {
        try {
            waitUntilCartVisible();
        } catch (Exception ignored) {}
        List<WebElement> items = driver.findElements(cartItems);
        System.out.println("DEBUG: removeItemByName scanning " + items.size() + " items for: " + partialName);
        for (WebElement it : items) {
            String title = "";
            try { title = it.findElement(itemNameInCart).getText().trim(); } catch (Exception e) { title = it.getText(); }
            if (!title.toLowerCase().contains(partialName.toLowerCase())) continue;

            // Keep an effectively-final copy for use inside lambdas
            final String titleFinal = title;

            // find remove controls scoped to this item
            List<By> removeLocs = List.of(
                    By.cssSelector(".shelf-item__del"),
                    By.cssSelector(".shelf-item_del"),
                    By.cssSelector("button.remove, .remove"),
                    By.cssSelector(".item-remove"),
                    By.cssSelector("button")
            );

            WebElement toClick = null;
            for (By loc : removeLocs) {
                List<WebElement> found = it.findElements(loc);
                if (!found.isEmpty()) {
                    for (WebElement e : found) {
                        try {
                            if (e.isDisplayed() && e.isEnabled()) {
                                toClick = e;
                                break;
                            }
                        } catch (Exception ignored) {}
                    }
                }
                if (toClick != null) break;
            }

            if (toClick == null) {
                // fallback: click an 'svg' or small x inside item
                try {
                    WebElement svg = it.findElement(By.cssSelector("svg"));
                    toClick = svg;
                } catch (Exception ignored) {}
            }

            if (toClick == null) {
                System.err.println("DEBUG: removeItemByName - no remove control found inside item: " + titleFinal);
                return false;
            }

            try {
                wait.until(ExpectedConditions.elementToBeClickable(toClick)).click();
            } catch (Exception e) {
                // JS fallback
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", toClick);
            }

            // wait until that specific item is no longer present
            try {
                boolean gone = new WebDriverWait(driver, java.time.Duration.ofSeconds(6)).until(d -> {
                    List<WebElement> now = d.findElements(cartItems);
                    for (WebElement e : now) {
                        try {
                            String t = e.findElement(itemNameInCart).getText().trim();
                            if (t.equalsIgnoreCase(titleFinal)) return false; // still present
                        } catch (Exception ignore) {}
                    }
                    return true; // not found
                });
                System.out.println("DEBUG: removeItemByName - item '" + titleFinal + "' disappeared = " + gone);
                return gone;
            } catch (Exception ex) {
                System.err.println("DEBUG: removeItemByName - wait for disappearance timed out: " + ex.getMessage());
                return false;
            }
        }
        System.err.println("DEBUG: removeItemByName - no matching item found for: " + partialName);
        return false;
    }


    // debug raw cart text
    public String getCartRawText() {
        try {
            waitForCartItems();
            List<WebElement> items = driver.findElements(cartItems);
            StringBuilder sb = new StringBuilder();
            for (WebElement it : items) {
                sb.append("---- ITEM ----\n").append(it.getText()).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
