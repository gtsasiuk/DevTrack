package com.example.devtrack.service;

import com.example.devtrack.model.Project;
import com.example.devtrack.model.User;
import com.example.devtrack.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {
    @Mock
    private ProjectRepository projectRepository;
    @InjectMocks
    private ProjectService projectService;
    private User user;
    private Project project;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).build();

        project = Project.builder()
                .id(1L)
                .user(user)
                .status(Project.Status.ACTIVE)
                .totalPrice(1000L)
                .advancePayment(500L)
                .clientName("Client A")
                .deadline(LocalDate.now().plusDays(5))
                .creationDate(LocalDateTime.now().minusDays(2))
                .build();
    }

    @Test
    void testAddProject() {
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        Project saved = projectService.add(project);

        assertEquals(project, saved);
        verify(projectRepository).save(project);
    }

    @Test
    void testFindById_WhenExists() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        Project found = projectService.findById(1L);

        assertEquals(project, found);
    }

    @Test
    void testFindById_WhenNotExists() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> projectService.findById(1L));
    }

    @Test
    void testFindAll() {
        when(projectRepository.findAll()).thenReturn(List.of(project));

        List<Project> projects = projectService.findAll();

        assertEquals(1, projects.size());
    }

    @Test
    void testFindAllByUser_ShouldMarkExpired() {
        Project expired = Project.builder()
                .id(2L)
                .user(user)
                .status(Project.Status.ACTIVE)
                .deadline(LocalDate.now().minusDays(1))
                .build();

        when(projectRepository.findByUserAndStatus(user, Project.Status.ACTIVE))
                .thenReturn(List.of(expired));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArguments()[0]);
        when(projectRepository.findByUser(user)).thenReturn(List.of(expired));

        List<Project> result = projectService.findAllByUser(user);

        assertEquals(1, result.size());
        assertEquals(Project.Status.EXPIRED, result.get(0).getStatus());
    }

    @Test
    void testFindByStatus_ShouldReturnOnlyMatching() {
        when(projectRepository.findByUserAndStatus(user, Project.Status.COMPLETED))
                .thenReturn(List.of(project));
        when(projectRepository.findByUserAndStatus(user, Project.Status.ACTIVE))
                .thenReturn(List.of());

        List<Project> result = projectService.findByStatus(user, Project.Status.COMPLETED);

        assertEquals(1, result.size());
        verify(projectRepository).findByUserAndStatus(user, Project.Status.COMPLETED);
    }

    @Test
    void testSortByDeadlines() {
        when(projectRepository.findByUserAndStatus(eq(user), eq(Project.Status.ACTIVE), any(Pageable.class)))
                .thenReturn(List.of(project));
        when(projectRepository.findByUserAndStatus(user, Project.Status.ACTIVE))
                .thenReturn(List.of());

        List<Project> sorted = projectService.sortByDeadlines(user);

        assertEquals(1, sorted.size());
    }


    @Test
    void testUpdateProject() {
        projectService.update(project);

        verify(projectRepository).save(project);
    }

    @Test
    void testDeleteProject() {
        projectService.delete(1L);

        verify(projectRepository).deleteById(1L);
    }

    @Test
    void testGetUserAchievements() {
        when(projectRepository.findByUserAndStatus(user, Project.Status.COMPLETED)).thenReturn(List.of(project));
        when(projectRepository.findByUser(user)).thenReturn(List.of(project));

        Map<String, Object> result = projectService.getUserAchievements(user);

        assertEquals("100.00", result.get("successRate"));
        assertEquals(1L, result.get("completedCount"));
        assertEquals(1000L, result.get("totalEarnings"));
    }

    @Test
    void testGetFullProfileStatistics() {
        when(projectRepository.findByUser(user)).thenReturn(List.of(project));
        when(projectRepository.findByUserAndStatus(user, Project.Status.COMPLETED)).thenReturn(List.of(project));
        when(projectRepository.findByUserAndStatus(user, Project.Status.ACTIVE)).thenReturn(List.of(project));
        when(projectRepository.findByUserAndStatus(user, Project.Status.CANCELLED)).thenReturn(List.of());
        when(projectRepository.findByUserAndStatus(user, Project.Status.EXPIRED)).thenReturn(List.of());

        Map<String, Object> result = projectService.getFullProfileStatistics(user);

        assertEquals(1L, result.get("completedCount"));
        assertTrue(result.containsKey("nearestDeadline"));
    }
}
