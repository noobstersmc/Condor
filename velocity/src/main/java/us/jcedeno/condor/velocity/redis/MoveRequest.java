package us.jcedeno.condor.velocity.redis;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MoveRequest {
    String player_name;
    String ip;
    String game_id;
    
}
