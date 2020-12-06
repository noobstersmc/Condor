package us.jcedeno.condor.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;
import us.jcedeno.condor.velocity.CondorVelocity;

/**
 * HubCommand
 */
@RequiredArgsConstructor
@CommandAlias("hub|lobby")
public class HubCommand extends BaseCommand {
    private @NonNull CondorVelocity instance;

    @Default
    public void onDefault(CommandSource source) {
        if (source instanceof Player) {
            var player = (Player) source;
            var lobby = instance.getServer().getServer("lobby");
            if (lobby.isPresent()) {
                var lobbyServer = lobby.get();
                if (player.getCurrentServer().get().getServerInfo().getName().equalsIgnoreCase("lobby")) {
                    player.spoofChatInput("/a");
                    return;
                }
                player.createConnectionRequest(lobbyServer).fireAndForget();
                player.sendMessage(TextComponent.of("Sending you to the lobby.").color(TextColor.GREEN));
            }
        }

    }

}