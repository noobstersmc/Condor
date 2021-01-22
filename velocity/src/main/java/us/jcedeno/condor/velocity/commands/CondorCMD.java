package us.jcedeno.condor.velocity.commands;

import java.io.IOException;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.velocitypowered.api.proxy.Player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Name;
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
        instance.getCommandManager().getCommandCompletions().registerAsyncCompletion("game-servers", c -> {
            return instance.getServer().getAllServers().stream()
                    .filter(server -> server.getPlayersConnected().size() > 0)
                    .map(server -> server.getServerInfo().getName()).collect(Collectors.toList());
        });
    }

    // @CommandCompletion("@game-servers")
    @CommandCompletion("@players default")
    @Subcommand("token")
    public void setToken(Player sender, @Name("condor-token") @Default("default") String token, boolean bol)
            throws IOException {
        var uuid = sender.getUniqueId().toString();
        if (token.equalsIgnoreCase("default")) {
            NewCondor.getTokenMap().remove(uuid);
        } else {
            NewCondor.getTokenMap().put(uuid, token);
        }
        var json = new JsonObject();
        json.addProperty("uuid", uuid);
        json.addProperty("token", NewCondor.getTokenMap().getOrDefault(uuid, uuid));
        if (bol)
            sender.sendMessage(TextComponent.of("Condor token has been updated to: " + token, TextColor.GREEN));

        NewCondor.postToken(json.toString());

    }

    @Subcommand("create")
    public void createServer(Player sender, @Name("Token") String token, @Name("json") String json) throws IOException {
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
        var list = gson.fromJson(
                NewCondor.getProfile(NewCondor.getTokenMap().getOrDefault(sender.getUniqueId().toString(), "null")),
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
                        .clickEvent(ClickEvent.runCommand("/lair delete " + game_id)));
            });
        } else {
            sender.sendMessage(TextComponent.of("You don't have any running instances.", TextColor.RED));
        }
    }

    @Subcommand("delete")
    public void deleteInstance(Player sender, String instance) throws IOException {

        var delete = gson.fromJson(NewCondor
                .delete(NewCondor.getTokenMap().getOrDefault(sender.getUniqueId().toString(), "null"), instance),
                JsonObject.class);

        sender.sendMessage(TextComponent.of(delete.toString()));
    }

    @Subcommand("remove this")
    public void deleteThis(Player sender) throws IOException {

        sender.sendMessage(TextComponent.of(sender.getCurrentServer().get().getServerInfo().getName()));

        var delete = NewCondor.delete("6QR3W05K3F", sender.getCurrentServer().get().getServerInfo().getName());

        sender.sendMessage(TextComponent.of(delete.toString()));
    }
}
