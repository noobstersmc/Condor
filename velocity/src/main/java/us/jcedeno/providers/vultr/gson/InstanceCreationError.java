package us.jcedeno.providers.vultr.gson;

import lombok.Data;

@Data
public class InstanceCreationError implements InstanceResult{
    String error;
    int status;
}
