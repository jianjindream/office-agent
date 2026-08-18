package com.jianjin.assistant.domain.sandbox;

public interface Executor {
    ExecResult exec(ExecRequest req);

    String backend();

    boolean available();
}
