package org.originit.executor;

import org.originit.config.CustomConfiguration;


public interface DeliverExecutor extends AutoCloseable {

    boolean checkCookie(CustomConfiguration customConfiguration);

    void tryLogin(CustomConfiguration customConfiguration);

    void execute(CustomConfiguration customConfiguration);

    void close();
}
