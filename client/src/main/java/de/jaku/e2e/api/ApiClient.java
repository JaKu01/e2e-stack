package de.jaku.e2e.api;

import de.jaku.e2e.util.PreKeyBundleJsonSerializer;
import lombok.extern.log4j.Log4j2;
import org.signal.libsignal.protocol.InvalidKeyException;
import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.protocol.state.PreKeyBundle;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

@Component
@Log4j2
public class ApiClient {

    public void uploadPreKeyBundle(String name, PreKeyBundle preKeyBundle) throws IOException, URISyntaxException, InterruptedException {

        String jsonBody = PreKeyBundleJsonSerializer.serializeToJson(preKeyBundle);

        HttpClient httpClient = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/keys?name=" + name))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        log.info("Uploaded prekey bundle, response: " + response.statusCode() + " " + response.body().toString());
    }

    public Boolean hasPreKeyBundle(String name) throws IOException, InterruptedException, URISyntaxException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/keys?name=" + name))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        HttpResponse response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();
        return statusCode >= 200 && statusCode < 300;
    }

    public PreKeyBundle fetchPreKeyBundle(String name) throws URISyntaxException, IOException, InterruptedException, InvalidKeyException {

        HttpClient httpClient = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/keys?name=" + name))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        HttpResponse response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return PreKeyBundleJsonSerializer.deserializeFromJson(response.body().toString());
    }

    public void sendMessage(SignalProtocolAddress to, byte[] message) throws URISyntaxException, IOException, InterruptedException {
        log.info("This message goes to " + to.getName() + "it is: " + Base64.getEncoder().encodeToString(message));

        String base64Message = Base64.getEncoder().encodeToString(message);


        HttpClient httpClient = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/messages?receiver=" + to.getName()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{ \"message\": \"" + base64Message +  "\"}"))
                .build();
        HttpResponse response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("Uploaded message for " + to.getName() + ", response: " + response.statusCode() + " " + response.body().toString());
    }

    public void receiveMessage(SignalProtocolAddress from) {

    }
}
