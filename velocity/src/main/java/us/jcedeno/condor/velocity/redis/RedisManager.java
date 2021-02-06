package us.jcedeno.condor.velocity.redis;

import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import lombok.Getter;
import net.kyori.text.TextComponent;
import net.kyori.text.event.ClickEvent;
import net.kyori.text.event.HoverEvent;
import net.kyori.text.event.HoverEvent.Action;
import net.kyori.text.format.TextColor;
import redis.clients.jedis.Jedis;
import us.jcedeno.condor.velocity.CondorVelocity;
import us.jcedeno.condor.velocity.commands.CondorCommand;

public class RedisManager {
    private CondorVelocity instance;
    private @Getter RedisClient redisClient;
    private @Getter StatefulRedisConnection<String, String> statefulRedisConnection;
    private @Getter RedisAsyncCommands<String, String> commands;
    private @Getter StatefulRedisPubSubConnection<String, String> connection;
    private @Getter Jedis jedis;

    private static Gson gson = new Gson();

    public RedisManager(CondorVelocity instance) {
        this.instance = instance;
        this.jedis = new Jedis("redis-11764.c73.us-east-1-2.ec2.cloud.redislabs.com", 11764);
        this.jedis.connect();
        this.jedis.auth("Gxb1D0sbt3VoyvICOQKC8IwakpVdWegW");

        this.redisClient = RedisClient.create(
                "redis://Gxb1D0sbt3VoyvICOQKC8IwakpVdWegW@redis-11764.c73.us-east-1-2.ec2.cloud.redislabs.com:11764/0");
        this.connection = redisClient.connectPubSub();
        this.statefulRedisConnection = redisClient.connect();
        this.commands = statefulRedisConnection.async();
        this.connection.addListener(new RedisPubSubListener<String, String>() {

            @Override
            public void message(String channel, String message) {
                Nprocess(channel, message);
                System.out.println(message);

            }

            @Override
            public void message(String pattern, String channel, String message) {
                // TODO Auto-generated method stub

            }

            @Override
            public void subscribed(String channel, long count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void psubscribed(String pattern, long count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void unsubscribed(String channel, long count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void punsubscribed(String pattern, long count) {
                // TODO Auto-generated method stub

            }

        });
        RedisPubSubAsyncCommands<String, String> async = connection.async();
        async.subscribe("condor");

    }

    void Nprocess(String Channel, String message) {
        if (Channel.equalsIgnoreCase("condor")) {
            var condor_action = gson.fromJson(message, JsonObject.class);
            var action_type = condor_action.get("type").getAsString();

            switch (action_type.toLowerCase()) {
                case "connect": {
                    var uuid = condor_action.get("uuid");
                    var condor_id = condor_action.get("condor_id");

                    if (uuid != null && condor_id != null) {
                        var player_query = instance.getServer().getPlayer(UUID.fromString(uuid.getAsString()));

                        if (player_query.isPresent()) {
                            var player = player_query.get();
                            var condorID = condor_id.getAsString();
                            var optional_server = instance.getServer().getServer(condorID);

                            if (optional_server.isPresent()) {
                                var server = optional_server.get();
                                player.createConnectionRequest(server).fireAndForget();

                            } else {
                                var ip = condor_action.get("ip").getAsString().split(":");
                                var server = instance.getServer()
                                        .registerServer(CondorCommand.of(condorID, ip[0], Integer.parseInt(ip[1])));
                                player.createConnectionRequest(server).fireAndForget();
                            }

                        }

                    }
                    break;
                }
                case "move": {
                    var target = condor_action.get("target").getAsString();
                    var source = condor_action.get("source").getAsString();
                    var players = condor_action.get("players").getAsString();
                    if (source != null && target != null) {
                        instance.getServer().getServer(source).ifPresent(sv -> {
                            instance.getServer().getServer(target).ifPresent(targetServer -> {
                                if (players.equals("@a")) {
                                    sv.getPlayersConnected()
                                            .forEach(all -> all.createConnectionRequest(targetServer).fireAndForget());

                                }

                            });
                        });

                    }
                    break;
                }
                case "moveOne": {

                    var target = condor_action.get("target").getAsString();
                    var uuid = UUID.fromString(condor_action.get("uuid").getAsString());
                    var ins = instance.getServer();
                    // Send player to server if it exists.
                    ins.getPlayer(uuid).ifPresent(player -> ins.getServer(target)
                            .ifPresent(server -> player.createConnectionRequest(server).fireAndForget()));

                    break;
                }
                case "broadcast": {
                    instance.getServer().broadcast(TextComponent.of(condor_action.get("message").getAsString()));
                    break;
                }
                case "refresh": {
                    instance.getServer().getAllServers().stream()
                            .filter(all -> all.getPlayersConnected().size() <= 0
                                    && !all.getServerInfo().getName().startsWith("lobby"))
                            .forEach(all -> instance.getServer().unregisterServer(all.getServerInfo()));
                    break;
                }
                // TODO: Allow condor to send more complex messages.
                case "notify": {
                    // Notify when a server has gone online
                    var component = TextComponent.of(condor_action.get("message").getAsString())
                            .hoverEvent(HoverEvent.of(Action.SHOW_TEXT,
                                    TextComponent.of("Click to join!", TextColor.GREEN)))
                            .clickEvent(ClickEvent.of(net.kyori.text.event.ClickEvent.Action.RUN_COMMAND,
                                    condor_action.get("command").getAsString()));
                    var target = condor_action.get("target");
                    if (target.isJsonArray()) {
                        var players = target.getAsJsonArray();
                        // Send message to specific users
                        players.forEach(all -> instance.getServer().getPlayer(UUID.fromString(all.getAsString()))
                                .ifPresent(present -> present.sendMessage(component)));
                    } else if (target.getAsString().startsWith("@a"))
                        // Send message to all users
                        instance.getServer().broadcast(component);

                    break;
                }
            }

        }

    }

}
