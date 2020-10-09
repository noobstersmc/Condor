package us.jcedeno.providers.vultr.gson;

import lombok.Data;

@Data
public class InstanceType implements InstanceResult {
    String id;
    String os;
    int ram;
    int disk;
    String main_ip;
    int vcpu_count;
    String region;
    String plan;
    String date_created;
    String status;
    int allowed_bandwidth;
    String gateway_v4;
    String label;
    String tag;
}
