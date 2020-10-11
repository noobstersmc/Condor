package us.jcedeno.condor.communicator.rest;

import java.util.UUID;

import org.bukkit.entity.Player;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SerializedPlayer {
    String name;
    UUID uuid;

    public static SerializedPlayer from(Player player){
        return SerializedPlayer.builder().name(player.getName()).uuid(player.getUniqueId()).build();
    }
}
