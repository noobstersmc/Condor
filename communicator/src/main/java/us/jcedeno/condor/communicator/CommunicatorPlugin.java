package us.jcedeno.condor.communicator;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.stop;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import spark.Route;
import spark.Spark;
import us.jcedeno.condor.communicator.rest.SerializedPlayer;

public class CommunicatorPlugin extends JavaPlugin {
    private @Getter Metrics metrics;
    private Gson gson = new GsonBuilder().create();

    @Override
    public void onEnable() {
        // Connect to metrics
        this.metrics = new Metrics(this, 9077);
        // Create config
        this.saveDefaultConfig();
        // Create RestAPI server on port 8080
        port(getConfig().getInt("port", 8081));
        get("/players", (req, res) -> gson.toJson(getSerializedPlayers(new ArrayList<>(Bukkit.getOnlinePlayers()))));
        

    }

    public static void addRoute(String path, Route route){
        Spark.get(path, route);
        System.out.println("Added route to path" + path);

    }


    List<SerializedPlayer> getSerializedPlayers(ArrayList<Player> players) {
        var serialedPlayers = new ArrayList<SerializedPlayer>();
        players.forEach(all -> serialedPlayers.add(SerializedPlayer.from(all)));

        return serialedPlayers;
    }

    @Override
    public void onDisable() {
        stop();
    }

}
