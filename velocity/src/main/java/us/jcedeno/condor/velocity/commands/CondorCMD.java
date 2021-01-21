package us.jcedeno.condor.velocity.commands;

import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.velocitypowered.api.proxy.Player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import us.jcedeno.condor.velocity.CondorVelocity;

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

    @CommandCompletion("@game-servers")
    @Default
    public void condorConnect(Player sender) {

    }
}
