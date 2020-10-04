package us.jcedeno.condor.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;

import org.slf4j.Logger;

@Plugin(id = "condor", name = "Condor Velocity", version = "0.1-SNAPSHOT",
        description = "Plugin that allows condor to add servers to the proxy.", authors = {"Juan Cedeno"})
public class CondorVelocity {
    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public CondorVelocity(ProxyServer server, Logger logger){
        this.server = server;
        this.logger = logger;
        logger.info("Condor has landed!");
    }
    
}
