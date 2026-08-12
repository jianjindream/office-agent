package com.jianjin.assistant.infrastructure.platform;

import com.jianjin.assistant.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Neo4jConnector {

    private static final Logger log = LoggerFactory.getLogger(Neo4jConnector.class);

    private final AppConfig cfg;
    private volatile String status = "managed-by-kgstore";

    public Neo4jConnector(AppConfig cfg) {
        this.cfg = cfg;
    }

    /** 由 KGStore 在启动后回调，告知是否可用。 */
    public void reportStatus(boolean available) {
        status = available ? "connected" : "disconnected";
    }

    public String status() { return status; }
}
