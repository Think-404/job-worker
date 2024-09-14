package org.originit.executor;

import org.originit.config.CustomConfiguration;


public interface DeliverExecutor extends AutoCloseable {

    void execute(CustomConfiguration customConfiguration);

    void close();
}
