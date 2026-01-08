package com.project.back_end.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Patient;
import com.project.back_end.services.MvcService;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.TokenService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/patient")
public class PatientController {

    private final TokenService tokenService;
    private final PatientService patientService;
    private final MvcService service;

    public PatientController(PatientService patientService, MvcService service, TokenService tokenService) {
        this.patientService = patientService;
        this.service = service;
        this.tokenService = tokenService;
    }

    @GetMapping("/{token}")
    public ResponseEntity<?> getPatient(@PathVariable String token) {
        String error = service.validateToken(token, "patient");
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        Patient patient = service.getPatientFromToken(token);
        return ResponseEntity.ok(patient);
    }

    @PostMapping("/register")
    public ResponseEntity<?> createPatient(@Valid @RequestBody Patient patient) {
        if (!service.validatePatient(patient.getEmail(), patient.getPhone())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Patient already exists.");
        } else {
            if (patientService.createPatient(patient) != null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating patient.");
            } else {
                return ResponseEntity.status(HttpStatus.CREATED).body("Patient registered successfully.");
            }
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Login login) {
        // Ruf die Service-Methode auf und gib das ResponseEntity direkt zurück
        return service.validatePatientLogin(login.getEmail(), login.getPassword());
    }


    @GetMapping("/appointments/{patientId}/{token}/{user}")
    public ResponseEntity<?> getPatientAppointment(@PathVariable Long patientId,
                                                   @PathVariable String token,
                                                   @PathVariable String user) {
        String error = service.validateToken(token, user);
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        return ResponseEntity.ok(
                patientService.getPatientAppointment(patientId)
        );
    }

    @GetMapping("/appointments/filter/{condition}/{doctorName}/{token}")
    public ResponseEntity<?> filterPatientAppointment(@PathVariable String condition,
                                                      @PathVariable String doctorName,
                                                      @PathVariable String token) {
        String error = service.validateToken(token, "patient");
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        String email = tokenService.extractEmail(token);
        return ResponseEntity.ok(
                patientService.filterAppointmentsByConditionAndDoctor(email, condition, doctorName)
        );
    }
    
// 1. Set Up the Controller Class:
//    - Annotate the class with `@RestController` to define it as a REST API controller for patient-related operations.
//    - Use `@RequestMapping("/patient")` to prefix all endpoints with `/patient`, grouping all patient functionalities under a common route.


// 2. Autowire Dependencies:
//    - Inject `PatientService` to handle patient-specific logic such as creation, retrieval, and appointments.
//    - Inject the shared `Service` class for tasks like token validation and login authentication.


// 3. Define the `getPatient` Method:
//    - Handles HTTP GET requests to retrieve patient details using a token.
//    - Validates the token for the `"patient"` role using the shared service.
//    - If the token is valid, returns patient information; otherwise, returns an appropriate error message.


// 4. Define the `createPatient` Method:
//    - Handles HTTP POST requests for patient registration.
//    - Accepts a validated `Patient` object in the request body.
//    - First checks if the patient already exists using the shared service.
//    - If validation passes, attempts to create the patient and returns success or error messages based on the outcome.


// 5. Define the `login` Method:
//    - Handles HTTP POST requests for patient login.
//    - Accepts a `Login` DTO containing email/username and password.
//    - Delegates authentication to the `validatePatientLogin` method in the shared service.
//    - Returns a response with a token or an error message depending on login success.


// 6. Define the `getPatientAppointment` Method:
//    - Handles HTTP GET requests to fetch appointment details for a specific patient.
//    - Requires the patient ID, token, and user role as path variables.
//    - Validates the token using the shared service.
//    - If valid, retrieves the patient's appointment data from `PatientService`; otherwise, returns a validation error.


// 7. Define the `filterPatientAppointment` Method:
//    - Handles HTTP GET requests to filter a patient's appointments based on specific conditions.
//    - Accepts filtering parameters: `condition`, `name`, and a token.
//    - Token must be valid for a `"patient"` role.
//    - If valid, delegates filtering logic to the shared service and returns the filtered result.



}


