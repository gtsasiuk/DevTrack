package com.example.devtrack.service;

import com.example.devtrack.model.Role;
import com.example.devtrack.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RoleServiceTest {
    @Mock
    private RoleRepository roleRepository;
    @InjectMocks
    private RoleService roleService;
    private Role sampleRole;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleRole = Role.builder().id(1L).name("ROLE_USER").build();
    }

    @Test
    void testAdd() {
        when(roleRepository.save(sampleRole)).thenReturn(sampleRole);
        Role saved = roleService.add(sampleRole);
        assertEquals(sampleRole, saved);
    }

    @Test
    void testFindById_RoleExists() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(sampleRole));
        Role found = roleService.findById(1L);
        assertEquals("ROLE_USER", found.getName());
    }

    @Test
    void testFindById_RoleNotFound() {
        when(roleRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> roleService.findById(1L));
    }

    @Test
    void testFindByName_RoleExists() {
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(sampleRole));
        Role result = roleService.findByName("ROLE_USER");
        assertEquals("ROLE_USER", result.getName());
    }

    @Test
    void testFindByName_RoleNotFound() {
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> roleService.findByName("ROLE_USER"));
    }

    @Test
    void testFindAll() {
        when(roleRepository.findAll()).thenReturn(List.of(sampleRole));
        List<Role> result = roleService.findAll();
        assertEquals(1, result.size());
    }

    @Test
    void testUpdate() {
        roleService.update(sampleRole);
        verify(roleRepository).save(sampleRole);
    }

    @Test
    void testDelete() {
        roleService.delete(1L);
        verify(roleRepository).deleteById(1L);
    }
}
