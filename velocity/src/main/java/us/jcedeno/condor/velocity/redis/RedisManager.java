package us.jcedeno.condor.velocity.redis;

import com.google.gson.Gson;

import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import lombok.Getter;
import net.kyori.text.TextComponent;
import us.jcedeno.condor.velocity.CondorVelocity;
import us.jcedeno.condor.velocity.commands.CondorCommand;

public class RedisManager {
    private CondorVelocity instance;
    private @Getter RedisClient redisClient;
    private @Getter StatefulRedisPubSubConnection<String, String> connection;
    private static Gson gson = new Gson();

    public RedisManager(CondorVelocity instance) {
        this.instance = instance;
        this.redisClient = RedisClient.create(
                "redis://Gxb1D0sbt3VoyvICOQKC8IwakpVdWegW@redis-11764.c73.us-east-1-2.ec2.cloud.redislabs.com:11764/0");
        this.connection = redisClient.connectPubSub();
        this.connection.addListener(new RedisPubSubListener<String, String>() {

            @Override
            public void message(String channel, String message) {
                process(channel, message);

            }

            @Override
            public void message(String arg0, String arg1, String arg2) {
                // TODO Auto-generated method stub

            }

            @Override
            public void psubscribed(String arg0, long arg1) {
                // TODO Auto-generated method stub

            }

            @Override
            public void punsubscribed(String arg0, long arg1) {
                // TODO Auto-generated method stub

            }

            @Override
            public void subscribed(String arg0, long arg1) {
                // TODO Auto-generated method stub

            }

            @Override
            public void unsubscribed(String arg0, long arg1) {
                // TODO Auto-generated method stub

            }

        });
        RedisPubSubAsyncCommands<String, String> async = connection.async();
        async.subscribe("condor-transfer");

    }

    void process(String Channel, String message) {
        if (Channel.equalsIgnoreCase("condor-transfer")) {
            var req = gson.fromJson(message, MoveRequest.class);
            var query = instance.getServer().getServer(req.game_id);
            var player_query = instance.getServer().getPlayer(req.getPlayer_name());
            if (player_query.isEmpty()) {
                return;
            }

            if (query.isPresent()) {
                var server = query.get();
                var actual_player = player_query.get();
                actual_player
                        .sendMessage(TextComponent.of("Attempting to connect " + server.getServerInfo().getName()));
                actual_player.createConnectionRequest(server).fireAndForget();
            } else {
                var server = instance.getServer()
                        .registerServer(CondorCommand.of(req.game_id, req.getIp(), req.getPort()));
                var actual_player = player_query.get();
                actual_player
                        .sendMessage(TextComponent.of("Attempting to connect " + server.getServerInfo().getName()));
                actual_player.createConnectionRequest(server).fireAndForget();
            }

        }

    }

}
