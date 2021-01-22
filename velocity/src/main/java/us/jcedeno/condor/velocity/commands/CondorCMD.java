package us.jcedeno.condor.velocity.commands;

import java.io.IOException;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.velocitypowered.api.proxy.Player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Name;
import co.aikar.commands.annotation.Subcommand;
import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;
import us.jcedeno.condor.velocity.CondorVelocity;
import us.jcedeno.condor.velocity.condor.NewCondor;

@CommandAlias("lair|condor|cv|condor-velocity|lair-velocity")
public class CondorCMD extends BaseCommand {

    private CondorVelocity instance;
    private static Gson gson = new Gson();

    public CondorCMD(CondorVelocity instance) {
        this.instance = instance;
        instance.getCommandManager().getCommandCompletions().registerAsyncCompletion("game-servers", c -> {
            return instance.getServer().getAllServers().stream()
                    .filter(server -> server.getPlayersConnected().size() > 0)
                    .map(server -> server.getServerInfo().getName()).collect(Collectors.toList());
        });
    }

    // @CommandCompletion("@game-servers")
    @CommandCompletion("default")
    @Subcommand("token")
    public void setToken(Player sender, @Name("condor-token") @Default("default") String token) {
        var uuid = sender.getUniqueId().toString();
        if (token.equalsIgnoreCase("default")) {
            NewCondor.getTokenMap().remove(uuid);
        } else {
            NewCondor.getTokenMap().put(uuid, token);
        }
        sender.sendMessage(TextComponent.of("Condor token has been updated to: " + token, TextColor.GREEN));

    }

    @Subcommand("create")
    public void createServer(Player sender, String json) throws IOException {
        var createServer = NewCondor.post(NewCondor.getTokenMap().getOrDefault(sender.getUniqueId().toString(), "none"),
                json);
        var json_reponse = gson.fromJson(createServer, JsonObject.class);
        var error = json_reponse.get("error");
        var game_id = json_reponse.get("game_id");
        if (error != null) {
            sender.sendMessage(TextComponent.of("Error: " + error.getAsString(), TextColor.RED));
            // TODO: Send sound to player.
        } else if (game_id != null) {
            sender.sendMessage(TextComponent.of("Created server " + game_id.getAsString(), TextColor.GREEN));

        }
    }
}
