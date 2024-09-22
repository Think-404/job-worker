package org.originit.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DeliveryErrorCode implements IErrorCode {
    DELIVERY_FINISHED("DELIVERY_FINISHED");

    @Getter
    private final String code;

    @Override
    public String getModule() {
        return "delivery";
    }
}
