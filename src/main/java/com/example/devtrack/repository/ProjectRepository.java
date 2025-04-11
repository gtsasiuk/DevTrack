package com.example.devtrack.repository;

import com.example.devtrack.model.Project;
import com.example.devtrack.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByUser(User user);
    List<Project> findByUserAndStatus(User user, Project.Status status);
    List<Project> findByUserAndStatus(User user, Project.Status status, Pageable pageable);
    List<Project> findByUser(User user, Sort sort);
}
