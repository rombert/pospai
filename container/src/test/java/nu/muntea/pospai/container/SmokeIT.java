package nu.muntea.pospai.container;

import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class SmokeIT {
	
	@Test
	public void ensurePageIsAccessible() throws IOException, InterruptedException {
		
		var httpClient = HttpClient.newHttpClient();
		
		String port = System.getenv("HTTP_PORT");
		if ( port == null )
			port = "8080";
		
		var consoleRequest = HttpRequest.newBuilder()
			.uri(URI.create("http://localhost:" + port + "/content/pospai/home/welcome.html")).build();
		
		HttpResponse<InputStream> bundleStatus = httpClient.send(consoleRequest, BodyHandlers.ofInputStream());
		
		assertThat("welcome page status code", bundleStatus.statusCode(), CoreMatchers.is(200));
	}

}
