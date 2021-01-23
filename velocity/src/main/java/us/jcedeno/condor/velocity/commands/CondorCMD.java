package us.jcedeno.condor.velocity.commands;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Name;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import net.kyori.text.TextComponent;
import net.kyori.text.event.ClickEvent;
import net.kyori.text.event.HoverEvent;
import net.kyori.text.format.TextColor;
import us.jcedeno.condor.velocity.CondorVelocity;
import us.jcedeno.condor.velocity.condor.NewCondor;

@CommandAlias("lair|condor|cv|condor-velocity|lair-velocity")
public class CondorCMD extends BaseCommand {

    private CondorVelocity instance;
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public CondorCMD(CondorVelocity instance) {
        this.instance = instance;
        instance.getCommandManager().getCommandContexts().registerContext(RegisteredServer.class, c -> {
            var arg = c.popFirstArg();
            var server_query = instance.getServer().getServer(arg);
            if (server_query.isEmpty())
                throw new InvalidCommandArgument("Sever " + arg + " doesn't exist.");

            return server_query.get();
        });
        instance.getCommandManager().getCommandCompletions().registerAsyncCompletion("gameservers", c -> {
            return getGameServers();
        });
    }

    public List<String> getGameServers() {
        return instance.getServer().getAllServers().stream().map(e -> e.getServerInfo().getName())
                .collect(Collectors.toList());
    }

    // @CommandCompletion("@game-servers")
    @CommandCompletion("default")
    @Subcommand("token")
    public void setToken(Player sender, @Name("condor-token") @Default("default") String token, @Optional Boolean bool)
            throws IOException {
        var uuid = sender.getUniqueId().toString();
        if (token.equalsIgnoreCase("default")) {
            NewCondor.getTokenMap().put(uuid, uuid);
        } else {
            try {
                var tokenize = UUID.fromString(token);
                if (tokenize != null && !sender.hasPermission("condor.token.uuid"))
                    return;
            } catch (Exception e) {
            }
            NewCondor.getTokenMap().put(uuid, token);
        }
        var json = new JsonObject();
        json.addProperty("uuid", uuid);
        json.addProperty("token", NewCondor.getTokenMap().getOrDefault(uuid, uuid));
        if (bool != null && bool)
            sender.sendMessage(TextComponent.of("Condor token has been updated to: " + token, TextColor.GREEN));

        NewCondor.postToken(json.toString());

    }

    @Subcommand("create")
    public void createServer(Player sender, @Name("Token") String token, @Name("template") String template_id)
            throws IOException {
        var json = NewCondor.getTemplate(template_id);
        if (json.equals("error")) {
            sender.sendMessage(TextComponent.of("This server has already been created.", TextColor.RED));
            return;
        }
        var createServer = NewCondor.post(token, json);
        var json_reponse = gson.fromJson(createServer, JsonObject.class);
        var error = json_reponse.get("error");
        var game_id = json_reponse.get("game_id");
        if (error != null) {
            sender.sendMessage(TextComponent.of("Error: " + error.getAsString(), TextColor.RED));
            // TODO: Send sound to player.
        } else if (game_id != null) {
            sender.sendMessage(TextComponent.of("Created server " + game_id.getAsString(), TextColor.GREEN));
            setToken(sender, token, false);

        }
    }

    @Subcommand("list")
    public void listActiveServers(Player sender) throws IOException {
        var list = gson.fromJson(NewCondor.getProfile(
                NewCondor.getTokenMap().getOrDefault(sender.getUniqueId().toString(), sender.getUniqueId().toString())),
                JsonObject.class);
        var instances = list.getAsJsonArray("instances");
        if (instances != null) {
            sender.sendMessage(TextComponent.of("Active instances: ", TextColor.GOLD));
            instances.forEach(all -> {
                var condor_instance = all.getAsJsonObject();
                var game_id = condor_instance.get("game_id").getAsString();
                var cfg = condor_instance.getAsJsonObject("request").getAsJsonObject("config");
                sender.sendMessage(TextComponent.of("- Game " + game_id)
                        .hoverEvent(HoverEvent.showText(TextComponent.of("Config: " + cfg.toString())
                                .append(TextComponent.of("\nClick to delete!", TextColor.GOLD))))
                        .clickEvent(ClickEvent.runCommand("/close " + game_id)));
            });
        } else {
            sender.sendMessage(TextComponent.of("You don't have any running instances.", TextColor.RED));
        }
    }

    @CommandPermission("condor.admin.pull")
    @Subcommand("admin pull")
    public void pullTokens(CommandSource source) throws IOException {
        var json = gson.fromJson(NewCondor.getTokens(), JsonObject.class);
        System.out.println(json.toString());
        json.entrySet().forEach(entries -> {
            var value = entries.getValue().getAsString();
            NewCondor.getTokenMap().put(entries.getKey(), value);
        });
    }

    @CommandPermission("condor.admin.refresh")
    @Subcommand("admin refresh")
    public void refreshServers(CommandSource source) throws IOException {
        instance.getServer().getAllServers().stream().filter(
                all -> all.getPlayersConnected().size() <= 0 && !all.getServerInfo().getName().startsWith("lobby"))
                .forEach(all -> instance.getServer().unregisterServer(all.getServerInfo()));
    }

    @CommandPermission("condor.remove.own")
    @CommandCompletion("@gameservers")
    @Subcommand("close")
    @CommandAlias("close")
    public void destroyInstanceCommand(CommandSource source, @Name("sever-name") @Optional RegisteredServer server)
            throws Exception {
        var token = "";
        if (server == null && source instanceof Player) {
            var player = ((Player) source);
            server = player.getCurrentServer().get().getServer();
            token = NewCondor.getTokenMap().getOrDefault(player.getUniqueId().toString(),
                    player.getUniqueId().toString());
        } else if (source.hasPermission("condor.remove.staff")) {
            token = "6QR3W05K3F";
        }
        destroyInstance(source, server, token);
    }

    public void destroyInstance(CommandSource source, RegisteredServer server, String token) throws Exception {
        var delete = NewCondor.delete(token, server.getServerInfo().getName());
        var result = gson.fromJson(delete.toString(), JsonObject.class);
        if (result != null) {
            var error = result.get("error");
            if (error != null) {
                source.sendMessage(TextComponent.of(error.getAsString(), TextColor.RED));
                return;
            }
            var response = result.get("result");
            if (response != null) {
                if (response.getAsString().equalsIgnoreCase("ok")) {

                    source.sendMessage(TextComponent.of(
                            "Instance " + server.getServerInfo().getName() + " has been deleted!", TextColor.GREEN));
                    instance.getServer().unregisterServer(server.getServerInfo());
                    var lobby = this.instance.getServer().getServer("lobby");
                    if (lobby.isPresent()) {
                        server.getPlayersConnected().stream().forEach(all -> {
                            all.createConnectionRequest(lobby.get()).connect().thenAccept(completed -> all
                                    .sendMessage(TextComponent.of("You've been moved to the lobby.", TextColor.GRAY)));

                        });
                    }
                } else {
                    source.sendMessage(TextComponent.of(response.getAsString(), TextColor.GREEN));
                }
            }

        }

    }
}
