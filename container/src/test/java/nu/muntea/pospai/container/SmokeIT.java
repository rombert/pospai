package nu.muntea.pospai.container;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class SmokeIT {
	
	@Test
	public void ensureConsoleIsAccessible() throws IOException, InterruptedException {
		
		String authPayload = Base64.getEncoder().encodeToString("admin:admin".getBytes(StandardCharsets.UTF_8));
		
		var httpClient = HttpClient.newHttpClient();
		
		String port = System.getenv("HTTP_PORT");
		if ( port == null )
			port = "8080";
		
		var consoleRequest = HttpRequest.newBuilder()
			.header("Authorization", "Basic " + authPayload)
			.uri(URI.create("http://localhost:" + port + "/system/console/bundles.json")).build();
		
		HttpResponse<InputStream> bundleStatus = httpClient.send(consoleRequest, BodyHandlers.ofInputStream());
		
		assertThat("bundles.json status code", bundleStatus.statusCode(), CoreMatchers.is(200));
		
		List<String> problematicBundles = new ArrayList<>();
		try ( var parser = Json.createReader(bundleStatus.body()) ) {
			JsonObject root = parser.readObject();
			JsonArray bundles = root.getJsonArray("data");
			for ( JsonValue bundle : bundles ) {
				String bundleSymbolicName = bundle.asJsonObject().getString("symbolicName");
				String state = bundle.asJsonObject().getString("state");
				
				if ( ! "Active".equals(state) && ! "Fragment".equals(state) ) {
					problematicBundles.add(bundleSymbolicName + " is in unexpected state '" + state); 
				}
			}
		}
		
		if ( ! problematicBundles.isEmpty() )
			fail(problematicBundles.toString());
	}

}
