package us.jcedeno.condor.velocity.redis;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MoveRequest {
    String player_name;
    String ip;
    String game_id;

    public String getIp() {
        return ip.split(":")[0];
    }

    public int getPort() {
        return Integer.parseInt(ip.split(":")[1]);
    }

}
