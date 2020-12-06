package us.jcedeno.condor.velocity.redis;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeleteRequest {
    String sender;
    String ip;
    String provider;
    public static String console_id = "fabed635-3a22-4b75-83ef-c44e3ba55c48";
    public static DeleteRequest of(String sender, String ip){
        return new DeleteRequest(sender, ip, "vultr");
    }
    public static DeleteRequest of(String ip){
        return new DeleteRequest(console_id, ip, "vultr");
    }
}
