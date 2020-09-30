package us.jcedeno.condor;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;

import org.slf4j.Logger;

@Plugin(id = "condor", name = "The Condor Project", version = "0.1-ALPHA", description = "A cloud provisioner plugin for velocity.", authors = {"Juan Cedeno"})
public class Condor {
    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public Condor(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;

        logger.info("Hello world from Condor");
    }
    
}
