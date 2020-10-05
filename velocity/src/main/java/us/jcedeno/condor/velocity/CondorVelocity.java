package us.jcedeno.condor.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;

import org.slf4j.Logger;

import co.aikar.commands.VelocityCommandManager;
import lombok.Getter;
import us.jcedeno.condor.velocity.commands.CondorCommand;

@Plugin(id = "condor", name = "Condor Velocity", version = "0.1-SNAPSHOT", description = "Plugin that allows condor to add servers to the proxy.", authors = {
        "Juan Cedeno" })
public class CondorVelocity {
    private @Getter ProxyServer server;
    private @Getter VelocityCommandManager commandManager;
    private final Logger logger;

    @Inject
    public CondorVelocity(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
        this.commandManager = new VelocityCommandManager(server, this);
        this.commandManager.registerCommand(new CondorCommand(this));
        logger.info("Condor has landed!");
    }

}
