package us.jcedeno.providers.vultr.gson;

import java.util.Base64;

import lombok.Data;

@Data
public class InstanceCreatorJson {
    String region = "ewr";
    String plan = "vhf-3c-8gb";
    String label = "UHC";
    String os_id = "352";
    String script_id = "5bd20686-41e3-4950-8424-55b477ab83c4";
    String user_data;
    String sshkey_id[] = new String[]{
        "f1bf11fd-6b87-450f-a2f3-c7c26e783144",
        "0dd4f40e-3e7a-4a28-a27e-ff3fde54d2d9",
        "5fa2fc79-84e2-43fe-9ed3-6e93d012623c"};

    public void setRun(){
        this.script_id = "720ef984-b58f-48a9-b4ba-113c8ce00dae";
        this.plan = "vhf-2c-4gb";
        this.label = "UHC-Run";
    }

    public void setSeed(String seed){
        user_data = Base64.getEncoder().encodeToString(("{\"seed\": \"level-seed=" + seed + "\"}").getBytes());

    }
    
}
