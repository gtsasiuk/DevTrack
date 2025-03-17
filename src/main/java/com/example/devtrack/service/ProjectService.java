package com.example.devtrack.service;

import com.example.devtrack.model.Project;
import com.example.devtrack.model.User;
import com.example.devtrack.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public Map<String, Object> getUserAchievements(User user) {
        List<Project> projects = projectRepository.findByUserAndStatus(user, Project.Status.COMPLETED);

        BigDecimal totalEarnings = projects.stream()
                .map(Project::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long completedCount = projects.size();

        BigDecimal avgProjectCost = completedCount > 0 ?
                totalEarnings.divide(BigDecimal.valueOf(completedCount), RoundingMode.HALF_UP) : BigDecimal.ZERO;

        BigDecimal maxProjectCost = projects.stream()
                .map(Project::getTotalPrice)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        long uniqueClients = projects.stream()
                .map(Project::getClientName)
                .distinct()
                .count();

        long totalProjects = projectRepository.findByUser(user).size();
        double successRate = totalProjects > 0 ? (double) completedCount / totalProjects * 100 : 0;

        Map<String, Object> achievements = new HashMap<>();
        achievements.put("totalEarnings", totalEarnings);
        achievements.put("completedCount", completedCount);
        achievements.put("avgProjectCost", avgProjectCost);
        achievements.put("maxProjectCost", maxProjectCost);
        achievements.put("uniqueClients", uniqueClients);
        achievements.put("successRate", successRate);

        return achievements;
    }

    public void update(Project project) {
        projectRepository.save(project);
    }

    public void delete(Long id) {
        projectRepository.deleteById(id);
    }
}
