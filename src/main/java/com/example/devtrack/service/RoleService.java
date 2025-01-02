package com.example.devtrack.service;

import com.example.devtrack.model.Role;
import com.example.devtrack.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class RoleService {
    private RoleRepository roleRepository;

    private Role add(Role role) {
        return roleRepository.save(role);
    }

    private Role findById(long id) {
        return roleRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("Role with ID " + id + " not found"));
    }

    private List<Role> findAll() {
        return roleRepository.findAll();
    }

    private void update(Role role) {
        roleRepository.save(role);
    }

    private void delete(long id) {
        roleRepository.deleteById(id);
    }
}
