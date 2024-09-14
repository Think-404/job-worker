package org.originit.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DriverErrorCode implements IErrorCode {
    PLATFORM_NOT_SUPPORTED("PlatformNotSupport"),
    ;

    private final String code;

    @Override
    public String getModule() {
        return "driver";
    }
}
