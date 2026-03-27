package com.security.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RestController
@CrossOrigin(origins = "*")
public class PictureController {

    @GetMapping("/redirect/{id}/i.jpg")
    public ResponseEntity<byte[]> redirect(@PathVariable String id) throws IOException, InterruptedException {
        String url = String.format("https://static.cricbuzz.com/a/img/v1/i1/c%s/i.jpg",id);
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Referer", "https://www.cricbuzz.com")
                .GET()
                .build();

        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

        return ResponseEntity.ok()
                .header("Content-Type", "image/jpg")
                .header("Access-Control-Allow-Origin", "*")  // allows Flutter web to access
                .body(response.body());

    }
}
