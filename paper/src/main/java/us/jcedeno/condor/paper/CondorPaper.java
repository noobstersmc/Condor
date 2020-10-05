package us.jcedeno.condor.paper;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;

import org.bukkit.plugin.java.JavaPlugin;

import co.aikar.commands.PaperCommandManager;
import fr.mrmicky.fastinv.FastInvManager;
import lombok.Getter;

/**
 * CondorPaper
 */
public class CondorPaper extends JavaPlugin {
    private @Getter Azure azure;
    private @Getter PaperCommandManager commandManager;

    @Override
    public void onEnable() {
        commandManager = new PaperCommandManager(this);
        FastInvManager.register(this);
        
        commandManager.registerCommand(new GuiCommand(this));

        try {
            var credentials = new ApplicationTokenCredentials("ae428b5b-3c74-453f-84bf-47f77155db5a",
                    "06ebbd6f-d9be-4f3b-aa71-bec887ccb7b0", "2E_6tGuY3Vw03KOzHSLOa4V9R~a.d3Lu1D",
                    AzureEnvironment.AZURE);

            azure = Azure.configure().authenticate(credentials)
                    .withSubscription("fcb53ea3-4077-45a3-9a6d-c8fd42c64372");

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}