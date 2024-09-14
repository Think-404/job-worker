package org.originit.utils;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Constant {
    public static ChromeDriver CHROME_DRIVER;
    public static Actions ACTIONS;
    public static WebDriverWait WAIT;
    public static WebDriverWait SHORT_WAIT;
    public static final int SHORT_WAIT_TIME = 5;
    public static final int WAIT_TIME = 60;
    public static String UNLIMITED_CODE = "0";
}
