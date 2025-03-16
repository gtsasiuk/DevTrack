package com.example.devtrack.service;

import com.example.devtrack.model.Project;
import com.example.devtrack.model.User;
import com.example.devtrack.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public List<Project> findAllByUser(User user) {
        return projectRepository.findByUser(user);
    }

    public List<Project> sortByDeadlines(User user) {
        Pageable pageable = PageRequest.of(0, 3, Sort.by(Sort.Order.asc("deadline")));
        return projectRepository.findByUserAndStatus(user, Project.Status.ACTIVE, pageable);
    }

    public void update(Project project) {
        projectRepository.save(project);
    }

    public void delete(Long id) {
        projectRepository.deleteById(id);
    }
}
