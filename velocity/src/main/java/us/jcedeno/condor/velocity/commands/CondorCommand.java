package us.jcedeno.condor.velocity.commands;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Name;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import net.kyori.text.event.HoverEvent;
import net.kyori.text.event.HoverEvent.Action;
import us.jcedeno.condor.velocity.CondorVelocity;
import us.jcedeno.providers.vultr.gson.InstanceCreationError;
import us.jcedeno.providers.vultr.gson.InstanceType;
import us.jcedeno.providers.vultr.gson.VultrAPI;

@RequiredArgsConstructor
@CommandPermission("condor.proxy")
@CommandAlias("condor-velocity|cv")
public class CondorCommand extends BaseCommand {
    private @NonNull CondorVelocity instance;
    private ArrayList<RegisteredServer> servers = new ArrayList<>();

    @Default
    public void onDefault(CommandSource source) {
        VultrAPI.getInstances().thenAccept(a -> {
            if (a != null) {
                Component text = TextComponent.of("Servers: ");
                for (var instance : a) {
                    text = text.append(TextComponent.of("vultr-" + instance.getMain_ip())).hoverEvent(HoverEvent.of(
                            Action.SHOW_TEXT,
                            TextComponent.of("ID: " + instance.getId() + "\nvCPU: " + instance.getVcpu_count()
                                    + "\nRAM: " + instance.getRam() + "M\nRegion: " + instance.getRegion()
                                    + "\nStatus: " + instance.getStatus() + "\nDate: " + instance.getDate_created()
                                    + "\nLabel: " + instance.getLabel() + "\nTag: " + instance.getTag())));
                }
                source.sendMessage(text);
            }
        });
    }

    @Subcommand("create uhc")
    public void onInstanceCreate(CommandSource source, @Default("ewr") @Name("region") String region,
            @Name("instance-type") @Default("vhf-3c-8gb") String instanceType) {
        var creatorName = source instanceof Player ? ((Player) source).getUsername() : "console";
        VultrAPI.createInstance(creatorName, region, instanceType).thenAccept((result) -> {
            if (result instanceof InstanceType) {
                var machine = (InstanceType) result;
                final var id = machine.getId();
                source.sendMessage(TextComponent.of("Instance is being created with id " + id));
                instance.getServer().getScheduler().buildTask(instance, () -> {
                    VultrAPI.getInstance(id).thenAccept((updatedResult) -> {
                        if (updatedResult instanceof InstanceType) {
                            var updatedMachine = (InstanceType) updatedResult;
                            servers.add(instance.getServer()
                                    .registerServer(of(updatedMachine.getId(), updatedMachine.getMain_ip(), 25565)));
                            instance.getServer().broadcast(TextComponent.of("Added server " + updatedMachine.getId()
                                    + " with ip " + updatedMachine.getMain_ip() + " to the proxy"));
                        } else {
                            source.sendMessage(TextComponent.of("An error ocurred..."));
                        }
                    });

                }).delay(10, TimeUnit.SECONDS).schedule();

            } else if (result instanceof InstanceCreationError) {
                var error = (InstanceCreationError) result;
                source.sendMessage(TextComponent.of("Instance couldn't be created with error " + error.getError()
                        + " and code " + error.getStatus()));
            } else {
                source.sendMessage(TextComponent.of("A null pointer exception has ocurred. Please report this."));
            }

        });
    }

    @Subcommand("add")
    public void onAddServer(CommandSource source, @Name("server-name") String name, @Name("ip-address") String ip,
            @Name("port") Integer port) {
        var serverInfo = of(name, ip, port);
        if (instance.getServer().getServer(name).isEmpty()) {
            servers.add(instance.getServer().registerServer(serverInfo));
            source.sendMessage(TextComponent.of("Registering server " + serverInfo.toString()));

        } else {
            source.sendMessage(TextComponent.of("Server already registered " + serverInfo.toString()));
        }
    }

    @Subcommand("remove ip")
    public void onRemoveIp(CommandSource source, @Name("ip") String ip) {
        if (ip != null) {
            var matchedInstance = instance.getServer().getAllServers().stream().filter(
                    server -> server.getServerInfo().getAddress().getAddress().getHostAddress().equalsIgnoreCase(ip))
                    .findFirst();
            if (matchedInstance.isPresent()) {
                var registeredServer = matchedInstance.get();
                var serverInfo = registeredServer.getServerInfo();
                servers.remove(registeredServer);

                instance.getServer().unregisterServer(serverInfo);
                source.sendMessage(TextComponent.of("Removed ephimeral instance " + serverInfo.toString()));
            } else {
                source.sendMessage(TextComponent.of("No ephimeral instance found with ip " + ip));
            }

        }
    }

    @Subcommand("remove name")
    public void onRemoveName(CommandSource source, @Name("server-name") String name,
            @Optional @Name("port") Integer port) {
        if (name != null) {
            var matchedInstance = instance.getServer().getServer(name);
            if (matchedInstance.isPresent()) {
                var registeredServer = matchedInstance.get();
                var serverInfo = registeredServer.getServerInfo();
                servers.remove(registeredServer);
                instance.getServer().unregisterServer(serverInfo);
                source.sendMessage(TextComponent.of("Removed ephimeral instance " + serverInfo.toString()));
            } else {
                source.sendMessage(TextComponent.of("No ephimeral instance named " + name));
            }

        }
    }

    @Subcommand("list")
    public void listServers(CommandSource source) {
        if (servers.isEmpty()) {
            source.sendMessage(TextComponent.of("No ephimeral instances have been added"));
        } else {
            servers.forEach(all -> source.sendMessage(TextComponent.of(all.getServerInfo().toString())));
        }

    }

    @Subcommand("restart")
    public void removeAllEphimeralInstances(CommandSource source) {
        if (servers.isEmpty()) {
            source.sendMessage(TextComponent.of("No ephimeral instances have been added"));
        } else {
            servers.forEach(all -> instance.getServer().unregisterServer(all.getServerInfo()));
            source.sendMessage(TextComponent.of("All ephimeral instances have been removed from the proxy."));
        }

    }

    @Subcommand("change")
    public void changeTemporary(CommandSource source, String serverName, String ip, @Optional Integer port) {
        var server = instance.getServer().getServer(serverName);
        if (server.isPresent()) {
            var sv = server.get();
            instance.getServer().unregisterServer(sv.getServerInfo());
            instance.getServer().registerServer(of(serverName, ip, port != null ? port : 25565));
            instance.getServer().getConfiguration().getAttemptConnectionOrder();
        } else {
            source.sendMessage(TextComponent.of(serverName + " doesn't exist."));
        }

    }

    @CommandAlias("pub")
    public void publish(CommandSource source, String channel, String message) {
        instance.getRedisManager().getConnection().publish(channel, message).thenAccept(a -> {
            System.out.println("PUBLISHED");
        });
    }

    @CommandAlias("sub")
    public void sub(CommandSource source, String channel) {
        instance.getRedisManager().getSub().subscribe(channel).thenAccept(a -> {
            source.sendMessage(TextComponent.of(a.toString() + ", " + channel));
        });
    }

    public static ServerInfo of(String name, String ip, int port) {
        return new ServerInfo(name, new InetSocketAddress(ip, port));
    }

}
