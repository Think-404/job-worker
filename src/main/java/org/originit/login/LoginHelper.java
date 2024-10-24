package org.originit.login;

import java.io.File;

public interface LoginHelper {

    boolean isLogin();

    File getLoginQRCode();

    boolean isQRCodeExpired(File qrCode);
}
