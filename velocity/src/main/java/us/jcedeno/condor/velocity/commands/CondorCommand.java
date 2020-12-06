package us.jcedeno.condor.velocity.commands;

import java.net.InetSocketAddress;

import com.google.gson.Gson;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
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

@CommandAlias("lv|lair|lair-velocity")
public class CondorCommand extends BaseCommand {
    private CondorVelocity instance;

    public CondorCommand(CondorVelocity instance) {
        this.instance = instance;
    }

    @Default
    public void onHelp(CommandSource source) {
        if (source.hasPermission("condor.host")) {

        } else if (source.hasPermission("condor.admin")) {

        } else {
            source.sendMessage(TextComponent.of("content"));

        }
    }

    @CommandPermission("condor.delete.game")
    @Subcommand("delete this")
    public void deleteCurrent(Player source) {
        if (source.getCurrentServer().isPresent()) {
            var server = source.getCurrentServer().get();
            var server_name = server.getServerInfo().getName();
            if (server_name.startsWith("game-")) {
                server.getServer().getPlayersConnected().forEach(all -> {
                    all.createConnectionRequest(server.getServer()).fireAndForget();
                    all.sendMessage(
                            TextComponent.of("Server " + server_name + " has been deleted. Thanks for playing."));
                });
                deleteInstance(source, server.getServerInfo().getName());
                return;
            }

        } else {
            source.sendMessage(TextComponent.of("Couldn't complete request."));
        }

    }

    private static Gson gson = new Gson();

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
            source.sendMessage(TextComponent.of(delete_json));

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
