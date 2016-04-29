import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mongodb.MongoClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class EndToEndTest {

    @Test
    public void doEndToEndTest() throws IOException {
        String serverHost = System.getProperty("serverHost");
        String dbHost = System.getProperty("dbHost");
        String insertUrl = String.format("http://%s:8080/control/add", serverHost);

        System.out.println("End to end test REST endpoint: " + insertUrl);
        System.out.println("End to end test db host: " + dbHost);

        JsonObject obj = new JsonObject();
        obj.addProperty("firstName", "first");
        obj.addProperty("lastName", "last");
        obj.addProperty("email", "email");

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(insertUrl);
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setEntity(new StringEntity(new Gson().toJson(obj)));
        CloseableHttpResponse result = httpClient.execute(httpPost);

        assertThat(result.getStatusLine().getStatusCode()).isEqualTo(200);

        MongoClient client = new MongoClient(dbHost);
        assertThat(client.getDB("test").getCollection("demoEntity").getCount()).isGreaterThan(0);
    }
}
