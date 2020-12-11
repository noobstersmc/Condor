package us.jcedeno.condor.velocity.commands;

import java.net.InetSocketAddress;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Name;
import co.aikar.commands.annotation.Subcommand;
import net.kyori.text.TextComponent;
import us.jcedeno.condor.velocity.CondorVelocity;
import us.jcedeno.condor.velocity.redis.DeleteRequest;
import us.jcedeno.condor.velocity.redis.ReinstallRequest;

@CommandAlias("lv|lair|lair-velocity")
public class CondorCommand extends BaseCommand {
    private CondorVelocity instance;
    private static Gson gson = new Gson();

    public CondorCommand(CondorVelocity instance) {
        this.instance = instance;
        instance.getCommandManager().getCommandCompletions().registerAsyncCompletion("game-servers", c -> {
            return instance.getServer().getAllServers().stream().filter(this::filter)
                    .map(r -> r.getServerInfo().getName()).collect(Collectors.toList());
        });
    }

    private boolean filter(RegisteredServer server) {
        return server.getServerInfo().getName().toLowerCase().startsWith("game-");
    }

    @Default
    public void onHelp(CommandSource source) {
        if (source.hasPermission("condor.host")) {

        } else if (source.hasPermission("condor.admin")) {

        } else {
            source.sendMessage(TextComponent.of("content"));

        }
    }

    @Subcommand("delete this")
    public void deleteCurrent(Player source) {
        if (source.hasPermission("condor.delete.game") || source.hasPermission("condor.delete.game.own")) {
            if (source.getCurrentServer().isPresent()) {
                var server = source.getCurrentServer().get();
                var server_name = server.getServerInfo().getName();
                if (server_name.startsWith("game-")) {
                    if (source.hasPermission("condor.delete.game") || shouldDeleteByHost(source, server)) {
                        var lobby = instance.getServer().getServer("lobby");
                        if (lobby.isPresent()) {
                            server.getServer().getPlayersConnected().forEach(all -> {
                                all.createConnectionRequest(lobby.get()).fireAndForget();
                                all.sendMessage(TextComponent.of("Server " + server_name + " has been deleted by "
                                        + source.getUsername() + ". Thanks for playing."));
                            });
                        }
                        deleteInstance(source, server.getServerInfo().getName());
                    }
                    return;
                }

            } else {
                source.sendMessage(TextComponent.of("Couldn't complete request."));
            }
        }
    }

    private boolean shouldDeleteByHost(Player source, ServerConnection server) {
        var lettuce = instance.getRedisManager().getCommands();
        try {
            var server_data = lettuce.keys("servers:*").get();
            if (server_data != null && !server_data.isEmpty()) {
                var m_server_data = lettuce.mget(server_data.toArray(new String[] {})).get();
                var optional_match = m_server_data.stream()
                        .filter(all -> all.getValue().toLowerCase().contains(getCommandSourceName(source)) && all
                                .getValue().toLowerCase().contains(server.getServerInfo().getAddress().getHostName()))
                        .findFirst();
                if (optional_match.isPresent()) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @CommandPermission("condor.delete.game")
    @Subcommand("delete game")
    public void deleteInstance(CommandSource source, @Name("instance-id") String id) {
        var optional_server = instance.getServer().matchServer(id).stream().findFirst();
        if (optional_server.isPresent()) {
            var server = optional_server.get();
            var source_name = getCommandSourceName(source);
            var delete_request = DeleteRequest.of(source_name, server.getServerInfo().getAddress().getHostName());
            var delete_json = gson.toJson(delete_request);
            instance.getRedisManager().getJedis().publish("destroy", delete_json);

        } else {
            source.sendMessage(TextComponent.of("Couldn't find an instance with id " + id));
        }

    }

    @CommandPermission("condor.delete.game")
    @Subcommand("reinstall this")
    public void reinstallCurrent(Player source) {
        if (source.getCurrentServer().isPresent()) {
            var server = source.getCurrentServer().get();
            var server_name = server.getServerInfo().getName();
            if (server_name.startsWith("game-")) {
                server.getServer().getPlayersConnected().forEach(all -> {
                    all.createConnectionRequest(server.getServer()).fireAndForget();
                    all.sendMessage(
                            TextComponent.of("Server " + server_name + " is being reinstalled. Thanks for playing."));
                });
                deleteInstance(source, server.getServerInfo().getName());
                return;
            }

        } else {
            source.sendMessage(TextComponent.of("Couldn't complete request."));
        }

    }

    @CommandPermission("condor.delete.game")
    @Subcommand("reinstall game")
    public void reinstallInstance(CommandSource source, @Name("instance-id") String id) {
        var optional_server = instance.getServer().matchServer(id).stream().findFirst();
        if (optional_server.isPresent()) {
            var server = optional_server.get();
            var source_name = getCommandSourceName(source);
            var reinstall_request = ReinstallRequest.of(source_name, server.getServerInfo().getAddress().getHostName());
            var reinstall_json = gson.toJson(reinstall_request);
            instance.getRedisManager().getJedis().publish("reinstall", reinstall_json);
            if (source.hasPermission("condor.developer"))
                source.sendMessage(TextComponent.of(reinstall_json));

        } else {
            source.sendMessage(TextComponent.of("Couldn't find an instance with id " + id));
        }

    }

    private String getCommandSourceName(CommandSource source) {
        return source instanceof Player ? ((Player) source).getUsername() : "console";
    }

    public static ServerInfo of(String name, String ip, int port) {
        return new ServerInfo(name, new InetSocketAddress(ip, port));
    }

}
