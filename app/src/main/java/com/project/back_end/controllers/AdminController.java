
package com.project.back_end.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.DTO.Login;
import com.project.back_end.services.MvcService;
@RestController
@RequestMapping("${api.path}admin")
public class AdminController {
    private final MvcService service;

    public AdminController(MvcService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody Login login) {
        // 1. Input-Validierung
        if (login == null || login.getUsername() == null || login.getPassword() == null) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "message", "Username and password are required.")
            );
        }

        try {
            // 2. Service-Aufruf mit Fehlerbehandlung
            Map<String, Object> result = service.validateAdmin(login.getUsername(), login.getPassword());

            // 3. Sichere Prüfung des "success"-Felds
            Boolean success = (Boolean) result.get("success");
            if (success == null || !success) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of(
                        "success", false,
                        "message", result.getOrDefault("message", "Login failed.")
                    )
                );
            }

            // 4. Erfolg: Nur notwendige Daten zurückgeben (keine internen Details)
            return ResponseEntity.ok(
                Map.of(
                    "success", true,
                    "message", "Login successful.",
                    "token", result.get("token"),
                    "claims", result.get("claims")
                )
            );

        } catch (Exception e) {
            // 5. Loggen und 500 Error zurückgeben
            System.err.println("Login error: " + e.getMessage()); // TODO: Logger verwenden
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Internal server error.")
            );
        }
    }


// 1. Set Up the Controller Class:
//    - Annotate the class with `@RestController` to indicate that it's a REST controller, used to handle web requests and return JSON responses.
//    - Use `@RequestMapping("${api.path}admin")` to define a base path for all endpoints in this controller.
//    - This allows the use of an external property (`api.path`) for flexible configuration of endpoint paths.


// 2. Autowire Service Dependency:
//    - Use constructor injection to autowire the `Service` class.
//    - The service handles core logic related to admin validation and token checking.
//    - This promotes cleaner code and separation of concerns between the controller and business logic layer.


// 3. Define the `adminLogin` Method:
//    - Handles HTTP POST requests for admin login functionality.
//    - Accepts an `Admin` object in the request body, which contains login credentials.
//    - Delegates authentication logic to the `validateAdmin` method in the service layer.
//    - Returns a `ResponseEntity` with a `Map` containing login status or messages.



}

