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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;

    public Project add(Project project) {
        return projectRepository.save(project);
    }

    public Project findById(long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Project with ID " + id + " not found"));
    }

    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    public List<Project> findAllByUser(User user) {
        markExpiredProjects(user);
        return projectRepository.findByUser(user);
    }

    public List<Project> findByStatus(User user, Project.Status status) {
        markExpiredProjects(user);
        return projectRepository.findByUserAndStatus(user, status);
    }

    public List<Project> sortByDeadlines(User user) {
        markExpiredProjects(user);
        Pageable pageable = PageRequest.of(0, 3, Sort.by(Sort.Order.asc("deadline")));
        return projectRepository.findByUserAndStatus(user, Project.Status.ACTIVE, pageable);
    }

    public List<Project> findAllSorted(User user, Sort sort) {
        markExpiredProjects(user);
        return projectRepository.findByUser(user, sort);
    }

    public void markExpiredProjects(User user) {
        List<Project> activeProjects = projectRepository.findByUserAndStatus(user, Project.Status.ACTIVE);
        LocalDate today = LocalDate.now();
        for (Project project : activeProjects) {
            if (project.getDeadline() != null && project.getDeadline().isBefore(today)) {
                project.setStatus(Project.Status.EXPIRED);
                projectRepository.save(project);
            }
        }
    }

    public Map<String, Object> getUserAchievements(User user) {
        List<Project> projects = projectRepository.findByUserAndStatus(user, Project.Status.COMPLETED);

        long totalEarnings = projects.stream()
                .mapToLong(Project::getTotalPrice)
                .sum();

        long completedCount = projects.size();

        long avgProjectCost = completedCount > 0 ? totalEarnings / completedCount : 0;

        long maxProjectCost = projects.stream()
                .mapToLong(Project::getTotalPrice)
                .max()
                .orElse(0);

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
        achievements.put("successRate", String.format(Locale.US, "%.2f", successRate));

        return achievements;
    }

    public Map<String, Object> getFullProfileStatistics(User user) {
        List<Project> allProjects = projectRepository.findByUser(user);
        List<Project> completedProjects = projectRepository.findByUserAndStatus(user, Project.Status.COMPLETED);
        List<Project> activeProjects = projectRepository.findByUserAndStatus(user, Project.Status.ACTIVE);
        List<Project> cancelledProjects = projectRepository.findByUserAndStatus(user, Project.Status.CANCELLED);
        List<Project> expiredProjects = projectRepository.findByUserAndStatus(user, Project.Status.EXPIRED);

        LocalDate today = LocalDate.now();

        long totalEarnings = completedProjects.stream().mapToLong(Project::getTotalPrice).sum();
        long completedCount = completedProjects.size();
        long avgProjectCost = completedCount > 0 ? totalEarnings / completedCount : 0;
        long maxProjectCost = completedProjects.stream().mapToLong(Project::getTotalPrice).max().orElse(0);
        long uniqueClients = completedProjects.stream().map(Project::getClientName).distinct().count();
        long totalProjects = allProjects.size();
        double successRate = totalProjects > 0 ? (double) completedCount / totalProjects * 100 : 0;

        long overdueProjects = expiredProjects.size();

        double avgDuration = completedProjects.stream()
                .filter(p -> p.getDeadline() != null)
                .mapToLong(p -> java.time.temporal.ChronoUnit.DAYS.between(p.getCreationDate().toLocalDate(), p.getDeadline()))
                .average().orElse(0);

        long withAdvancePayment = allProjects.stream()
                .filter(p -> p.getAdvancePayment() != null && p.getAdvancePayment() > 0)
                .count();

        long totalAdvance = allProjects.stream()
                .mapToLong(p -> p.getAdvancePayment() != null ? p.getAdvancePayment() : 0)
                .sum();

        long avgAdvancePayment = withAdvancePayment > 0 ? totalAdvance / withAdvancePayment : 0;

        long withLinks = allProjects.stream()
                .filter(p -> p.getProjectLink() != null && !p.getProjectLink().isBlank())
                .count();

        Optional<LocalDate> nearestDeadline = activeProjects.stream()
                .map(Project::getDeadline)
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo);

        long recentProjects = allProjects.stream()
                .filter(p -> p.getCreationDate() != null && p.getCreationDate().isAfter(today.minusMonths(1).atStartOfDay()))
                .count();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalEarnings", totalEarnings);
        stats.put("avgProjectCost", avgProjectCost);
        stats.put("maxProjectCost", maxProjectCost);
        stats.put("uniqueClients", uniqueClients);
        stats.put("successRate", String.format(Locale.US, "%.2f", successRate));
        stats.put("totalProjects", totalProjects);
        stats.put("activeProjects", activeProjects.size());
        stats.put("completedCount", completedCount);
        stats.put("cancelledProjects", cancelledProjects.size());
        stats.put("overdueProjects", overdueProjects);
        stats.put("avgDuration", String.format(Locale.US, "%.2f", avgDuration));
        stats.put("withAdvancePayment", withAdvancePayment);
        stats.put("avgAdvancePayment", avgAdvancePayment);
        stats.put("withLinks", withLinks);
        stats.put("nearestDeadline", nearestDeadline.orElse(null));
        stats.put("recentProjects", recentProjects);

        return stats;
    }

    public void update(Project project) {
        projectRepository.save(project);
    }

    public void delete(Long id) {
        projectRepository.deleteById(id);
    }
}
