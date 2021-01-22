package us.jcedeno.condor.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;

import org.slf4j.Logger;

import co.aikar.commands.VelocityCommandManager;
import lombok.Getter;
import us.jcedeno.condor.velocity.commands.CondorCMD;
import us.jcedeno.condor.velocity.commands.CondorCommand;
import us.jcedeno.condor.velocity.commands.HubCommand;
import us.jcedeno.condor.velocity.redis.RedisManager;

@Plugin(id = "condor", name = "Condor Velocity", version = "0.1-SNAPSHOT", description = "Plugin that allows condor to add servers to the proxy.", authors = {
        "Juan Cedeno" })
public class CondorVelocity {
    private @Getter ProxyServer server;
    private @Getter VelocityCommandManager commandManager;
    private @Getter RedisManager redisManager;
    private final @Getter Logger logger;

    @Inject
    public CondorVelocity(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
        logger.info("Condor has landed!");

    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.commandManager = new VelocityCommandManager(server, this);
        this.commandManager.registerCommand(new CondorCommand(this));
        this.commandManager.registerCommand(new HubCommand(this));
        this.commandManager.registerCommand(new CondorCMD(this));
        this.redisManager = new RedisManager(this);
        logger.info("Command has been injected.");
    }

}
