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

    public static CompletableFuture<InstanceResult> createInstance(String creator, String region, String instanceType) {
        return CompletableFuture.supplyAsync(() -> {
            var body = RequestBody.create(createServerJson(creator, region, instanceType), JSON);
            var request = new Request.Builder().url("https://api.vultr.com/v2/instances")
                    .header("Authorization", "Bearer " + VULTR_API_KEY).post(body).build();
                    
            InstanceResult result = null;

            try (Response response = client.newCall(request).execute()) {
                var responseJson = response.body().string();
                if (!response.isSuccessful()) {
                    result = gson.fromJson(responseJson, InstanceCreationError.class);
                }else{
                    result= gson.fromJson(gson.fromJson(responseJson, JsonElement.class).getAsJsonObject().get("instance"), InstanceType.class);
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
                    .header("Authorization", "Bearer AHEH4R54IO6BJ2LBSQTUXTV5YIW7UTTYYP5A").build();

            try (Response response = client.newCall(request).execute()) {
                var responseJson = response.body().string();
                if (!response.isSuccessful()) {
                    return gson.fromJson(responseJson, InstanceCreationError.class);
                }
                return gson.fromJson(responseJson, InstanceType.class);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    private static String createServerJson(String username, String region, String instanceType) {
        return String.format(
                "{\"region\": \"%s\", \"plan\":\"%s\", \"label\": \"%s game\", \"os_id\": \"352\", \"script_id\": \"5bd20686-41e3-4950-8424-55b477ab83c4\", \"sshkey_id\": [\"f1bf11fd-6b87-450f-a2f3-c7c26e783144\", \"0dd4f40e-3e7a-4a28-a27e-ff3fde54d2d9\", \"5fa2fc79-84e2-43fe-9ed3-6e93d012623c\"]}",
                region, instanceType, username);
    }

    public static void main(String[] args) {

        createInstance("juan2", "ewr", "vhf-1c-2gb").thenAcceptAsync(a -> {
            if (a instanceof InstanceType) {
                var ins = (InstanceType) a;
                System.out.println("Instance" + ins.getId());
            } else if (a instanceof InstanceCreationError) {
                var error = (InstanceCreationError) a;
                System.out.println("error: " + error.error);
            }else{
                System.err.println("error");
            }
        });
        while(true){

        }
    }

}
