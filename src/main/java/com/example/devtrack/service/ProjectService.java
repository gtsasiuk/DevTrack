package com.example.devtrack.service;

import com.example.devtrack.model.Project;
import com.example.devtrack.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;

    public Project add(Project project) {
        return projectRepository.save(project);
    }

    public Project findById(long id) {
        return projectRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("Project with ID " + id + " not found"));
    }

    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    public void update(Project project) {
        projectRepository.save(project);
    }

    public void delete(long id) {
        projectRepository.deleteById(id);
    }
}
