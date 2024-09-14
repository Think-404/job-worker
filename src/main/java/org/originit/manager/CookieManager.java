package org.originit.manager;

import org.openqa.selenium.WebDriver;

public interface CookieManager {

    void loadCookies(WebDriver driver, String cookiePath);

    void saveCookies(WebDriver driver, String cookiePath);
}
