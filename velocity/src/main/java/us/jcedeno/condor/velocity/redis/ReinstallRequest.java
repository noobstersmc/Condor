package us.jcedeno.condor.velocity.redis;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReinstallRequest {
    String sender;
    String ip;
    String provider;
    public static String console_id = "fabed635-3a22-4b75-83ef-c44e3ba55c49";
    public static ReinstallRequest of(String sender, String ip){
        return new ReinstallRequest(sender, ip, "vultr");
    }
    public static ReinstallRequest of(String ip){
        return new ReinstallRequest(console_id, ip, "vultr");
    }
}
