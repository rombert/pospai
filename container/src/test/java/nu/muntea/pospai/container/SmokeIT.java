package nu.muntea.pospai.container;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class SmokeIT {
	
	@Test
	public void ensurePageIsAccessible() throws IOException, InterruptedException {
		
		var httpClient = HttpClient.newHttpClient();
		
		String port = System.getenv("HTTP_PORT") != null ? System.getenv("HTTP_PORT") : "8080";
		
		await().atMost(180, SECONDS).until(() -> {
			var consoleRequest = HttpRequest.newBuilder()
					.uri(URI.create("http://localhost:" + port + "/content/pospai/home/welcome.html")).build();
				
				return httpClient.send(consoleRequest, BodyHandlers.ofInputStream()).statusCode();
		}, CoreMatchers.is(200));
	}

}
