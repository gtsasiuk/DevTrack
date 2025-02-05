package com.example.devtrack.service;

import com.example.devtrack.model.Role;
import com.example.devtrack.repository.RoleRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@AllArgsConstructor
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;

    public Role add(Role role) {
        return roleRepository.save(role);
    }

    public Role findById(long id) {
        return roleRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("Role with ID " + id + " not found"));
    }

    public Role findByName(String name) {
        return roleRepository.findByName(name).orElseThrow(() ->
                new NoSuchElementException("Role with name " + name + " not found"));
    }

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public void update(Role role) {
        roleRepository.save(role);
    }

    public void delete(long id) {
        roleRepository.deleteById(id);
    }
}
