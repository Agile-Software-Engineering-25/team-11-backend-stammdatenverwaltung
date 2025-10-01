package com.ase.stammdatenverwaltung.services;

import com.ase.stammdatenverwaltung.entities.User;
import com.ase.stammdatenverwaltung.repositories.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service // Indicates that this is a service component
public class UserService {

  private final UserRepository userRepository;

  // Spring automatically injects UserRepository due to @Service and constructor
  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<User> getAllUsers() {
    return userRepository.findAll();
  }

  public Optional<User> getUserById(Long id) {
    return userRepository.findById(id);
  }

  public User createUser(User user) {
    // You could add more business logic here, e.g., validation
    return userRepository.save(user);
  }

  public User updateUser(Long id, User updatedUser) {
    return userRepository
        .findById(id)
        .map(
            user -> {
              user.setUsername(updatedUser.getUsername());
              user.setEmail(updatedUser.getEmail());
              return userRepository.save(user);
            })
        .orElseThrow(
            () -> new RuntimeException("User not found with id " + id)); // Example error handling
  }

  public void deleteUser(Long id) {
    userRepository.deleteById(id);
  }

  public Optional<User> findUserByUsername(String username) {
    return userRepository.findByUsername(username);
  }
}
