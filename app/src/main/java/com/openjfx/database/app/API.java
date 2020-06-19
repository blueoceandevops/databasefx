package com.openjfx.database.app;

import com.openjfx.database.app.utils.AssetUtils;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.LogManager;

/**
 * Application internal request API interface
 *
 * @author yangkui
 * @since 1.0
 */
public class API {
    /**
     * Request host
     */
    public static final String HOST = "navigational.cn";
    /**
     * Request http type http or https
     */
    public static final String HTTP_PREFIX = "http://";
    /**
     * Http request port
     */
    public static final int HTTP_PORT = 8080;
    /**
     * Application request check update
     */
    public static final String CHECK_UPDATE = HTTP_PREFIX + HOST + ":" + HTTP_PORT + "/app/forUpdate";
    /**
     * Http request success status code
     */
    public static final int HTTP_OK = 200;

    private static final Logger LOG = LoggerFactory.getLogger(API.class);

    public static JsonObject checkUpdate() throws IOException, InterruptedException {
        var attr = AssetUtils.loadManifest();
        var uri = API.buildURIWithQuery(API.CHECK_UPDATE, "clientVersion", attr.get("Manifest-Version"));
        var request = HttpRequest.newBuilder(uri).build();
        var client = HttpClient.newHttpClient();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        var statusCode = response.statusCode();
        var body = response.body();
        final JsonObject json;
        if (statusCode != API.HTTP_OK) {
            json = null;
            LOG.error("Http request check update failed status code:{},body:{}", statusCode, body);
        } else {
            json = (JsonObject) Json.decodeValue(body);
        }
        return json;
    }

    public static URI buildURIWithQuery(String str, String key, String value) {
        var map = new HashMap<String, String>();
        map.put(key, value);
        return buildURIWithQuery(str, map);
    }

    public static URI buildURIWithQuery(String str, Map<String, String> queries) {
        Objects.requireNonNull(str);
        Objects.requireNonNull(queries);
        var i = 0;
        var sb = new StringBuilder(str);
        for (Map.Entry<String, String> entry : queries.entrySet()) {
            var key = entry.getKey();
            var val = entry.getValue();
            var query = key + "=" + val;
            if (i == 0) {
                sb.append("?").append(query);
            } else {
                sb.append("&").append(query);
            }
            i++;
        }
        var url = sb.toString();
        return URI.create(url);
    }
}
