package com.shashank.alumni.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shashank.alumni.dto.AlumniSearchRequest;
import com.shashank.alumni.model.Alumni;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PhantomBusterClient {

    @Value("${phantombuster.api.key}")
    private String apiKey;

    @Value("${phantombuster.agent.id}")
    private String agentId;

    @Value("${phantombuster.session.cookie}")
    private String sessionCookie;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public PhantomBusterClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public List<Alumni> searchAlumni(AlumniSearchRequest request) {
        String launchUrl = "https://api.phantombuster.com/api/v2/agents/launch";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Phantombuster-Key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);


        String searchStr = request.getUniversity();
        if (request.getDesignation() != null && !request.getDesignation().isEmpty()) {
            searchStr += " " + request.getDesignation();
        }
        if (request.getPassoutYear() != null && !request.getPassoutYear().toString().isEmpty()) {
            searchStr += " " + request.getPassoutYear();
        }

        Map<String, Object> argumentMap = Map.of(
            "search", searchStr,
            "sessionCookie", sessionCookie,
            "numberOfResultsPerLaunch", 50,
            "numberOfResultsPerSearch", 50
        );

        String argumentsJson = "";
        try {
             argumentsJson = objectMapper.writeValueAsString(argumentMap);
        } catch (Exception e) {
             throw new RuntimeException("Failed to serialize request arguments", e);
        }

        Map<String, Object> body = Map.of(
            "id", agentId,
            "argument", argumentsJson
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> launchResponse = restTemplate.postForEntity(launchUrl, entity, String.class);
            if (!launchResponse.getStatusCode().is2xxSuccessful() || launchResponse.getBody() == null) {
                throw new RuntimeException("Failed to launch PhantomBuster agent");
            }
            
            JsonNode responseNode = objectMapper.readTree(launchResponse.getBody());
            String containerId = responseNode.path("containerId").asText();
            
        
            JsonNode outputNode = pollForOutput(containerId, headers);
            
            return parseAlumniData(outputNode, headers, containerId);
        } catch (Exception e) {
            throw new RuntimeException("Error interacting with PhantomBuster API: " + e.getMessage(), e);
        }
    }

    private JsonNode pollForOutput(String containerId, HttpHeaders headers) throws Exception {
        String statusUrl = "https://api.phantombuster.com/api/v2/containers/fetch?id=" + containerId;
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        int maxRetries = 60; // 60 attempts
        int waitTimeMs = 5000; // 5 seconds per attempt

        for (int i = 0; i < maxRetries; i++) {
            Thread.sleep(waitTimeMs);
            ResponseEntity<String> response = restTemplate.exchange(statusUrl, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode responseNode = objectMapper.readTree(response.getBody());
                String status = responseNode.path("status").asText();
                if ("finished".equalsIgnoreCase(status)) {
                    int exitCode = responseNode.path("exitCode").asInt();
                    String outputUrl = "https://api.phantombuster.com/api/v2/containers/fetch-output?id=" + containerId;
                    ResponseEntity<String> outputResponse = restTemplate.exchange(outputUrl, HttpMethod.GET, entity, String.class);
                    String rawBody = outputResponse.getBody();
                    
                    if (exitCode != 0) {
                        System.err.println("PhantomBuster finished with error code " + exitCode);
                        System.err.println("Raw output logs: " + rawBody);
                    
                    }
                    
                    JsonNode outputNode = objectMapper.readTree(rawBody);
                    return outputNode;
                } else if ("error".equalsIgnoreCase(status)) {
                    throw new RuntimeException("PhantomBuster agent finished with error.");
                }
            }
        }
        
        throw new RuntimeException("Timeout waiting for PhantomBuster agent to finish.");
    }

    private List<Alumni> parseAlumniData(JsonNode outputNode, HttpHeaders headers, String containerId) {
        List<Alumni> result = new ArrayList<>();
        if (outputNode != null && outputNode.path("output").isTextual()) {
            String logs = outputNode.path("output").asText();
            

            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("https://[a-zA-Z0-9_./-]+\\.s3\\.amazonaws\\.com/[a-zA-Z0-9_./-]+/result\\.json");
            java.util.regex.Matcher matcher = pattern.matcher(logs);
            
            if (matcher.find()) {
                String jsonUrl = matcher.group();
                System.out.println("Detected JSON Results URL: " + jsonUrl);
                
                try {
                    String outputJson = restTemplate.getForObject(jsonUrl, String.class);
                    if (outputJson != null) {
                        JsonNode arrayNode = objectMapper.readTree(outputJson);
                        if (arrayNode.isArray()) {
                            for (JsonNode node : arrayNode) {
                                Alumni alumni = new Alumni();
                                alumni.setName(node.path("name").asText(null));
                                alumni.setCurrentRole(node.path("currentRole").asText(null) != null ? node.path("currentRole").asText() : node.path("job").asText(null));
                                alumni.setUniversity(node.path("university").asText(null) != null ? node.path("university").asText() : node.path("school").asText(null));
                                alumni.setLocation(node.path("location").asText(null));
                                alumni.setLinkedinHeadline(node.path("headline").asText(null));
                                
                                JsonNode yearNode = node.path("passoutYear");
                                if (yearNode.isMissingNode()) yearNode = node.path("schoolDateRange");
                                
                                if (yearNode.isNumber()) {
                                    alumni.setPassoutYear(yearNode.asInt());
                                } else if (yearNode.isTextual() && !yearNode.asText().isEmpty()) {
                                    try {
                                        String yearStr = yearNode.asText();
        
                                        if (yearStr.contains("-")) {
                                            yearStr = yearStr.substring(yearStr.indexOf("-") + 1).trim();
                                        }
                                        alumni.setPassoutYear(Integer.parseInt(yearStr));
                                    } catch (NumberFormatException ignored) {}
                                }
                                
                                alumni.setScrapedAt(java.time.LocalDateTime.now());
                                result.add(alumni);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error fetching or parsing results JSON from S3: " + e.getMessage());
                }
            } else {
                System.err.println("No JSON result URL found in PhantomBuster logs: \n" + logs);
                
                if (logs.contains("We've already retrieved all results from that search")) {
                    System.out.println("No new PhantomBuster agent results created because this exact search was already performed.");
                    System.out.println("Please check your database for the previously saved alumni from this search query.");
                }
            }
        }
        return result;
    }
}
