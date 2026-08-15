package com.payflow.payflow;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoadTest {
    public static void main(String[] args) {
        HttpClient client = HttpClient.newHttpClient();
        ExecutorService executor = Executors.newFixedThreadPool(15);

        for (int i = 0; i < 50; i++) {
            final int reqId = i;
            executor.submit(() -> {
                String payload = """
                    {
                        "customerId": "123e4567-e89b-12d3-a456-426614174000",
                        "amount": 250.00,
                        "idempotencyKey": "test-req-%d"
                    }
                    """.formatted(reqId);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/orders"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(payload))
                        .build();

                try {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    System.out.println("Status: " + response.statusCode());
                } catch (Exception e) {
                    System.out.println("Failed");
                }
            });
        }
        executor.shutdown();
    }
}