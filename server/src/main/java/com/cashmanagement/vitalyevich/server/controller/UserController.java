package com.cashmanagement.vitalyevich.server.controller;

import com.cashmanagement.vitalyevich.server.firebase.model.WorkTime;
import com.cashmanagement.vitalyevich.server.model.*;
import com.cashmanagement.vitalyevich.server.repository.*;
import com.cashmanagement.vitalyevich.server.service.FBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Controller
public class UserController {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final AccessRepository accessRepository;

    private final CompanyRepository companyRepository;

    private final BrigadeRepository brigadeRepository;

    public UserController(UserRepository userRepository, AccessRepository accessRepository, RoleRepository roleRepository, CompanyRepository companyRepository, BrigadeRepository brigadeRepository) {
        this.userRepository = userRepository;
        this.accessRepository = accessRepository;
        this.roleRepository = roleRepository;
        this.companyRepository = companyRepository;
        this.brigadeRepository = brigadeRepository;
    }

    @QueryMapping
    Iterable<User> users () {
        return userRepository.findByOrderByIdAsc();
    }

    @MutationMapping
    User createUser(@Argument User user, @Argument Role role) {
        Set<Role> roles = new LinkedHashSet<>();
        roles.add(role);
        user.setRoles(roles);
        return userRepository.save(user);
    }

    @MutationMapping
    User updateUser(@Argument User user, @Argument Role role) {
        Set<Role> roles = new LinkedHashSet<>();
        roles.add(role);
        user.setRoles(roles);
        return userRepository.save(user);
    }

    @MutationMapping
    void deleteUser(@Argument Integer id) {
        userRepository.deleteById(id);
    }

    @QueryMapping
    Iterable<Role> roles () {
        return  roleRepository.findAll();
    }

    @QueryMapping
    Optional<User> user (@Argument Integer id) {
        return userRepository.findUserById(id);
    }

    @QueryMapping
    Optional<Access> access (@Argument Integer id) {
        return accessRepository.findAccessById(id);
    }

    @MutationMapping
    Access updateAccess(@Argument Access access, @Argument User user) {
        access.setUser(user);
        return accessRepository.save(access);
    }

    @MutationMapping
    Access createAccess(@Argument Access access, @Argument User user) {
        access.setUser(user);
        return accessRepository.save(access);
    }

    @QueryMapping
    Iterable<Access> accesses () {
        return accessRepository.findByOrderByIdAsc();
    }

    @QueryMapping
    Iterable<Company> companies () {
        return  companyRepository.findAll();
    }

    @QueryMapping
    Iterable<Brigade> brigades () {
        return  brigadeRepository.findAll();
    }

    @Autowired
    FBService fbService;

    @MutationMapping
    void createWork(@Argument WorkTime work) throws ExecutionException, InterruptedException {
        fbService.saveWork(work);
    }
}
