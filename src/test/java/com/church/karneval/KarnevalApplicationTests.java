package com.church.karneval;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
class KarnevalApplicationTests {

	@Test
	void contextLoads() {
	}



	@Autowired
	private com.church.karneval.repository.UserRepository userRepository;
	@Autowired
	private com.church.karneval.repository.TeamRepository teamRepository;
	@Autowired
	private com.church.karneval.repository.StationRepository stationRepository;

	@Test
	void dumpDatabaseState() {
		System.out.println("=== TEAMS IN DB ===");
		teamRepository.findAll().forEach(t -> System.out.println("Team: " + t.getName() + " | Color: " + t.getColor() + " | ID: " + t.getId()));

		System.out.println("=== STATIONS IN DB ===");
		stationRepository.findAll().forEach(s -> System.out.println("Station: " + s.getName() + " | ID: " + s.getId()));

		System.out.println("=== USERS IN DB ===");
		userRepository.findAll().forEach(u -> System.out.println("User: " + u.getName() + " | Email: " + u.getEmail() + " | Role: " + u.getRole() + " | Status: " + u.getStatus() + " | ID: " + u.getId()));
	}

	@Test
	void testSupabaseConnection() {
		String signupUri = "https://iyddhrusrxyrcoyealur.supabase.co/auth/v1/signup";
		String anonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Iml5ZGRocnVzcnh5cmNveWVhbHVyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODA4MTg4OTQsImV4cCI6Mj96Mzk0ODk0fQ.MZFdwJG-3WELvklpHnEoI5oJN8F0xF71aINb4jJqhrg";
		String signupPayload = "{\"email\":\"test_temp_user@gmail.com\",\"password\":\"Password123!\"}";

		try {
			System.out.println("--- TESTING POST WITH JAVA HTTP CLIENT ---");
			long start = System.currentTimeMillis();
			HttpClient client = HttpClient.newBuilder()
					.version(HttpClient.Version.HTTP_1_1)
					.build();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(signupUri))
					.header("Content-Type", "application/json")
					.header("apikey", anonKey)
					.POST(HttpRequest.BodyPublishers.ofString(signupPayload))
					.build();
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			long end = System.currentTimeMillis();
			System.out.println("Java HttpClient Status: " + response.statusCode());
			System.out.println("Java HttpClient Time taken: " + (end - start) + " ms");
			System.out.println("Java HttpClient Body: " + response.body());
		} catch (Exception e) {
			System.out.println("Java HttpClient Failed!");
			e.printStackTrace();
		}

		try {
			System.out.println("--- TESTING POST WITH SPRING RESTTEMPLATE ---");
			long start = System.currentTimeMillis();
			RestTemplate restTemplate = new RestTemplate();
			org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
			headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
			headers.set("apikey", anonKey);
			org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(signupPayload, headers);
			org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity(signupUri, entity, String.class);
			long end = System.currentTimeMillis();
			System.out.println("RestTemplate Success! Time taken: " + (end - start) + " ms");
			System.out.println("RestTemplate Body: " + response.getBody());
		} catch (Exception e) {
			System.out.println("RestTemplate Failed!");
			e.printStackTrace();
		}
	}

}


