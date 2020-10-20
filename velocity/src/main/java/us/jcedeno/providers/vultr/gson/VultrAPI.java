package us.jcedeno.providers.vultr.gson;

import java.util.concurrent.CompletableFuture;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VultrAPI {
    private final static OkHttpClient client = new OkHttpClient();
    private final static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final static String VULTR_API_KEY = "RSBCD6OMAKB6TNWS7PUBLGCLWKNTD36U7HGA";
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public static CompletableFuture<InstanceType[]> getInstances() {
        return CompletableFuture.supplyAsync(() -> {
            var request = new Request.Builder().url("https://api.vultr.com/v2/instances")
                    .header("Authorization", "Bearer " + VULTR_API_KEY).build();

            InstanceType[] instances = null;

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful())
                    return instances;
                var ins = gson.fromJson(response.body().string(), JsonElement.class).getAsJsonObject().get("instances")
                        .getAsJsonArray();
                instances = gson.fromJson(ins, InstanceType[].class);
                return instances;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return instances;
        });
    }

    public static CompletableFuture<InstanceResult> createInstance(String creator, String seed, boolean run) {
        return CompletableFuture.supplyAsync(() -> {
            var game = new InstanceCreatorJson();
            game.setLabel(creator);
            if (run) {
                game.setRun();
            }
            if (seed != null && !seed.isBlank()) {
                game.setSeed(seed);
            }
            var body = RequestBody.create(gson.toJson(game), JSON);
            var request = new Request.Builder().url("https://api.vultr.com/v2/instances")
                    .header("Authorization", "Bearer " + VULTR_API_KEY).post(body).build();

            InstanceResult result = null;

            try (Response response = client.newCall(request).execute()) {
                var responseJson = response.body().string();
                if (!response.isSuccessful()) {
                    result = gson.fromJson(responseJson, InstanceCreationError.class);
                } else {
                    result = gson.fromJson(
                            gson.fromJson(responseJson, JsonElement.class).getAsJsonObject().get("instance"),
                            InstanceType.class);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        });
    }

    public static CompletableFuture<InstanceResult> getInstance(String id) {
        return CompletableFuture.supplyAsync(() -> {

            var request = new Request.Builder().url("https://api.vultr.com/v2/instances/" + id)
                    .header("Authorization", "Bearer " + VULTR_API_KEY).build();

            try (Response response = client.newCall(request).execute()) {
                var responseJson = response.body().string();
                if (!response.isSuccessful()) {
                    return gson.fromJson(responseJson, InstanceCreationError.class);
                }
                return gson.fromJson(gson.fromJson(responseJson, JsonElement.class).getAsJsonObject().get("instance"),
                        InstanceType.class);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public static CompletableFuture<InstanceCreationError> deleteInstance(String id) {
        return CompletableFuture.supplyAsync(() -> {
            var request = new Request.Builder().url("https://api.vultr.com/v2/instances/" + id).delete()
                    .header("Authorization", "Bearer " + VULTR_API_KEY).build();

            try (Response response = client.newCall(request).execute()) {
                var responseJson = response.body().string();
                if (!response.isSuccessful()) {
                    return gson.fromJson(responseJson, InstanceCreationError.class);
                }
                // TODO: Change it to Completion Event or something.
                return gson.fromJson("{\"error\": \"Ok. Completed succesfully\",\"status\": \"200\"}",
                        InstanceCreationError.class);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public static void main(String[] args) {
        var gs = new GsonBuilder().setPrettyPrinting().create();
        var instance = new InstanceCreatorJson();
        System.out.println(gs.toJson(instance));
        instance.setRun();
        instance.setSeed("puto");
        System.out.println(gs.toJson(instance));
    }

}
