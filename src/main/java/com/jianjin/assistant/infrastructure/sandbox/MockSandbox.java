package com.jianjin.assistant.infrastructure.sandbox;

import com.jianjin.assistant.domain.sandbox.ExecRequest;
import com.jianjin.assistant.domain.sandbox.ExecResult;
import com.jianjin.assistant.domain.sandbox.Executor;

public class MockSandbox implements Executor {

    @Override public String backend() { return "mock"; }
    @Override public boolean available() { return true; }

    @Override
    public ExecResult exec(ExecRequest req) {
        ExecResult r = new ExecResult();
        r.setCommand(req.getCommand());
        r.setBackend("mock");
        r.setStdout("[mock] 命令 \"" + req.getCommand()
                + "\" 在模拟沙箱中执行（Docker 不可用）");
        r.setExitCode(0);
        r.setDurationMs(1);
        return r;
    }
}
