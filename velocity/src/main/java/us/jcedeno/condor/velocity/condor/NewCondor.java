package us.jcedeno.condor.velocity.condor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import lombok.Getter;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NewCondor {
    private @Getter static final Map<String, String> tokenMap = new HashMap<>();
    private @Getter static final Map<String, String> customInstanceType = new HashMap<>();
    private @Getter static final Map<String, String> customWhitelistId = new HashMap<>();
    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    static OkHttpClient client = new OkHttpClient().newBuilder().readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS).build();
    public static String CONDOR_URL = "https://hynix-condor.herokuapp.com/";

    /**
     * Create a post request in condor.
     * 
     * @param auth Authorization token, also used as billing id.
     * @param json JSON with the specific request
     * @return Json Object in string form.
     * @throws IOException
     */
    public static String post(String auth, String json) throws IOException {
        var body = RequestBody.create(json, JSON);
        Request request = new Request.Builder().url(CONDOR_URL + "instances").addHeader("Authorization", auth)
                .addHeader("Content-Type", "application/json").post(body).build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    /**
     * Create a post request in condor.
     * 
     * @param auth Authorization token, also used as billing id.
     * @param json JSON with the specific request
     * @return Json Object in string form.
     * @throws IOException
     */
    public static String postToken(String json) throws IOException {
        var body = RequestBody.create(json, JSON);
        Request request = new Request.Builder().url(CONDOR_URL + "guis/token")
                .addHeader("Content-Type", "application/json").post(body).build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    /**
     * Create a post request in condor.
     * 
     * @param auth Authorization token, also used as billing id.
     * @param json JSON with the specific request
     * @return Json Object in string form.
     * @throws IOException
     */
    public static String getTokens() throws IOException {
        var body = RequestBody.create("{}", JSON);
        Request request = new Request.Builder().url(CONDOR_URL + "guis/token").post(body).build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    /**
     * Creates a get request to condor.
     * 
     * @param auth Authorization token
     * @param url  Path and parameters using ?
     * @return Result depends on path but expect a json.
     * @throws IOException
     */
    public static String get(String auth, String url) throws IOException {
        Request request = new Request.Builder().url(CONDOR_URL + url).addHeader("Authorization", auth)
                .addHeader("Content-Type", "application/json").get().build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    /**
     * Obtain a Condor User's profile status including outstanding credits, active
     * instances, instance limit, available credits, and array of instances.
     * 
     * @param auth User's auth token
     * @return JSON containing amounts, limit, and instances as JsonArray.
     * @throws IOException
     */
    public static String getProfile(String auth) throws IOException {
        return get(auth, "billing/status?onlyActive=true");
    }

    /**
     * Array of all available compute in a vultr region.
     * 
     * @param region Vultr regions, by default use EWR (NY)
     * @return Collection of instance types. May be empty but not null.
     */
    public static ArrayList<String> getAvailability(String region) {
        var list = new ArrayList<String>();
        try {
            var response = client.newCall(new Request.Builder()
                    .url("https://api.vultr.com/v2/regions/" + region + "/availability").get().build()).execute();
            var json = gson.fromJson(response.body().string(), JsonObject.class);
            var instances = json.getAsJsonArray("available_plans");
            if (instances != null)
                instances.forEach(e -> list.add(e.getAsString()));

        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Ask condor to tweet the given input
     * 
     * @param tweet Stringified tweet
     * @param auth  Authorization token for Condor-lair
     * @return Condor tweet response, including tweet url
     * @throws IOException
     */
    public static String tweet(String tweet, String auth) throws IOException {
        // Create the tweet as json
        var tweet_json = LairTweet.of(tweet).toJson(gson);
        // Create the request and call for response

        var body = RequestBody.create(tweet_json, JSON);
        Request request = new Request.Builder().url(CONDOR_URL + "utils/tweet").addHeader("Authorization", auth)
                .addHeader("Content-Type", "application/json").post(body).build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    /**
     * Create a delete request in condor
     * 
     * @param auth      Authorization token, also used as billing id.
     * @param condor_id Condor_id of instance to be deleted
     * @return Json Response in string form.
     * @throws IOException
     */
    public static String delete(String auth, String condor_id) throws IOException {
        Request request = new Request.Builder().url(CONDOR_URL + "instances/" + condor_id + "")
                .addHeader("Authorization", auth).addHeader("Content-Type", "application/json").delete().build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    /**
     * Return a Json array of an user's active instances.
     * 
     * @param auth Authorization token, use app api key.
     * @param id   billing id or token to look for.
     * @return JsonArray of active instances.
     * @throws IOException
     */
    public static String getBill(String auth, String id) throws IOException {
        Request request = new Request.Builder().url(CONDOR_URL + "bills/" + id + "?onlyActive=true")
                .addHeader("Authorization", auth).addHeader("Content-Type", "application/json").get().build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }

    }

    public static String postTemplate(String json) throws IOException {
        var body = RequestBody.create(json, JSON);
        Request request = new Request.Builder().url(CONDOR_URL + "utils/request")
                .addHeader("Content-Type", "application/json").post(body).build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public static String getTemplate(String id) throws IOException {
        Request request = new Request.Builder().url(CONDOR_URL + "utils/request?template_id=" + id)
                .addHeader("Content-Type", "application/json").get().build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }

    }

}
