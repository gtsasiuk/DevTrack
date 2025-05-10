package com.example.devtrack.controller;

import com.example.devtrack.model.Project;
import com.example.devtrack.model.User;
import com.example.devtrack.service.ProjectService;
import com.example.devtrack.service.UserService;
import com.example.devtrack.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;

import java.util.Collections;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(ProjectController.class)
public class ProjectControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ProjectService projectService;
    @MockBean
    private UserService userService;
    @MockBean
    private JwtUtil jwtUtil;
    private String token;
    private User user;
    private Project project;
    @MockBean
    private ThymeleafViewResolver thymeleafViewResolver;

    @BeforeEach
    void setUp() {
        token = "valid.token.here";
        user = User.builder().id(1L).username("testUser").email("test@example.com").build();
        project = Project.builder().id(1L).projectType("Test Project").user(user).build();

        when(jwtUtil.isValidToken(token)).thenReturn(true);
        when(jwtUtil.extractUsername(token)).thenReturn(user.getUsername());
        when(userService.findByUsername(user.getUsername())).thenReturn(user);
    }

    private MockHttpServletRequestBuilder withJwt(MockHttpServletRequestBuilder builder) {
        return builder.cookie(new jakarta.servlet.http.Cookie("jwt", token));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetAllProjects() throws Exception {
        when(projectService.findAllByUser(user)).thenReturn(Collections.singletonList(project));

        mockMvc.perform(withJwt(get("/projects")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("projects"))
                .andExpect(view().name("project/projects"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testShowCreateProjectForm() throws Exception {
        mockMvc.perform(withJwt(get("/projects/create")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("project", "user"))
                .andExpect(view().name("project/create_project"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCreateProject() throws Exception {
        mockMvc.perform(withJwt(post("/projects/create")
                        .with(csrf())
                        .param("title", "Test Project")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects"));

        verify(projectService).add(any(Project.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testEditProject() throws Exception {
        when(projectService.findById(1L)).thenReturn(project);

        mockMvc.perform(withJwt(get("/projects/edit/1")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("project"))
                .andExpect(view().name("project/edit_project"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdateProject() throws Exception {
        mockMvc.perform(withJwt(post("/projects/edit/1")
                        .with(csrf())
                        .param("title", "Updated Project")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects"));

        verify(projectService).update(any(Project.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDeleteProject() throws Exception {
        mockMvc.perform(withJwt(get("/projects/delete/1")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects"));

        verify(projectService).delete(1L);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testRedirectToLoginIfTokenInvalid() throws Exception {
        when(jwtUtil.isValidToken(token)).thenReturn(false);

        mockMvc.perform(withJwt(get("/projects")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
    }
}
