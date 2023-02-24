package org.example.config;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

public class Config {
    private String agentId;
    private final String languageId, pathKey, locationId, projectId, environmentId;

    public Config(Boolean enableProxy, String languageId, String pathKey, String locationId, String environmentId,
                  String agentId) {
        this.languageId = languageId;
        this.pathKey = pathKey;
        this.locationId = locationId;
        this.environmentId = environmentId;
        this.agentId = agentId;
        this.projectId = getProjectId(this.pathKey);

        if (enableProxy) {
            setProxy();
        }
    }

    private void setProxy() {
        //TODO add your proxy configuration
    }

    private String getProjectId(String pathJsonKey) {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = new JSONObject();

        try (FileReader reader = new FileReader(pathJsonKey)) {
            Object obj = parser.parse(reader);
            jsonObject = (JSONObject) obj;
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return (String) jsonObject.get("project_id");
    }

    public String getLanguageId() {
        return languageId;
    }

    public String getPathKey() {
        return pathKey;
    }

    public String getLocationId() {
        return locationId;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getEnvironmentId() {
        return environmentId;
    }

    public String getAgentId() {
        return agentId;
    }
}
