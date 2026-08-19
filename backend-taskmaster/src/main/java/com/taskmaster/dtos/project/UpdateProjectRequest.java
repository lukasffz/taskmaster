package com.taskmaster.dtos.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(min = 1, max = 200, message = "Project name must be between 1 and 200 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    public UpdateProjectRequest() {
    }

    public UpdateProjectRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
