package us.jcedeno.condor.velocity.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import lombok.Getter;
import net.kyori.text.TextComponent;
import us.jcedeno.condor.velocity.CondorVelocity;

public class RedisManager {
    private CondorVelocity instance;
    private @Getter RedisClient redisClient;
    private @Getter RedisPubSubAsyncCommands<String, String> connection;
    private @Getter RedisPubSubAsyncCommands<String, String> sub;

    public RedisManager(CondorVelocity instance){
        this.instance = instance;
        this.redisClient = RedisClient.create("redis://Gxb1D0sbt3VoyvICOQKC8IwakpVdWegW@redis-11764.c73.us-east-1-2.ec2.cloud.redislabs.com:11764/0");
        this.connection = redisClient.connectPubSub().async();
        this.sub = redisClient.connectPubSub().async();

        sub.getStatefulConnection().addListener(new RedisPubSubListener<String,String>(){

            @Override
            public void message(String channel, String message) {
                instance.getServer().broadcast(TextComponent.of(channel + ": " + message));
            }

            @Override
            public void message(String pattern, String channel, String message) {
            }

            @Override
            public void subscribed(String channel, long count) {
                instance.getServer().broadcast(TextComponent.of("Subscribed to " + channel + ": " + count));

            }

            @Override
            public void psubscribed(String pattern, long count) {

            }

            @Override
            public void unsubscribed(String channel, long count) {

            }

            @Override
            public void punsubscribed(String pattern, long count) {

            }
            
        });
        sub.subscribe("test");

    }

    
}
