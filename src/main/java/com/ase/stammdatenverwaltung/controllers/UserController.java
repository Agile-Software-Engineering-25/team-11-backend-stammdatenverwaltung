package com.ase.stammdatenverwaltung.controllers;

import com.ase.stammdatenverwaltung.entities.User;
import com.ase.stammdatenverwaltung.services.UserService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // Indicates that this is a REST controller
@RequestMapping("/user") // Base path for all endpoints in this controller
public class UserController {

  private final UserService userService;

  // Spring automatically injects UserService
  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public List<User> getAllUsers() {
    return userService.getAllUsers();
  }

  @GetMapping("/{userID}")
  public ResponseEntity<User> getUserById(@PathVariable Long id) {
    return userService
        .getUserById(id)
        .map(ResponseEntity::ok) // If user is found, return 200 OK with user
        .orElse(ResponseEntity.notFound().build()); // If not found, return 404 Not Found
  }

  // @GetMapping("/{userID}/role/preview")
  // public previewRole(){
  //    return
  // }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED) // Return 201 Created status
  public User createUser(@RequestBody User user) {
    return userService.createUser(user);
    // user hier an die Datenbank weitergeben (Gibts noch keine Datenbank)
  }

  @PutMapping("/{userID}")
  public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
    try {
      User updatedUser = userService.updateUser(id, userDetails);
      return ResponseEntity.ok(updatedUser);
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build(); // Or handle specifically with @ControllerAdvice
    }
    // updatedUser hier an die Datenbank weitergeben (Gibts noch keine Datenbank)
  }

  @DeleteMapping("/{userID}")
  @ResponseStatus(HttpStatus.NO_CONTENT) // Return 204 No Content status
  public void deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
    // id hier aus der Datenbank löschen (Gibts noch keine Datenbank)
  }

  @GetMapping("/username/{username}")
  public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
    return userService
        .findUserByUsername(username)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
