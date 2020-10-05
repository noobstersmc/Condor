package us.jcedeno.condor.velocity.commands;

import com.velocitypowered.api.command.CommandSource;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.text.TextComponent;
import us.jcedeno.condor.velocity.CondorVelocity;



@RequiredArgsConstructor
@CommandAlias("condor-velocity|cv")
public class CondorCommand extends BaseCommand{
    private @NonNull CondorVelocity instance;

    @Default
    public void onDefault(CommandSource source){
        source.sendMessage(TextComponent.of("Using acf"));
    }

}
