package com.project.back_end.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.models.Prescription;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.MvcService;
import com.project.back_end.services.PrescriptionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("${api.path}prescription")
public class PrescriptionController {
    private final PrescriptionService prescriptionService;
    private final MvcService service;
    private final AppointmentService appointmentService;

    public PrescriptionController(PrescriptionService prescriptionService,
                                  MvcService service,
                                  AppointmentService appointmentService) {
        this.prescriptionService = prescriptionService;
        this.service = service;
        this.appointmentService = appointmentService;
    }

    @PostMapping("/save/{token}")
    public ResponseEntity<?> savePrescription(@Valid @RequestBody Prescription prescription,
                                              @PathVariable String token) {
        String error = service.validateToken(token, "doctor");
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        try {
            prescriptionService.savePrescription(prescription);
            Long appointmentId = prescription.getAppointmentId();
            appointmentService.changeStatus(appointmentId, 2); // 2 = Status für "Prescription hinzugefügt"

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Prescription saved successfully.");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving prescription: " + e.getMessage());
        }
    }

    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<?> getPrescription(@PathVariable Long appointmentId,
                                             @PathVariable String token) {
        String error = service.validateToken(token, "doctor");
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    ResponseEntity<?> response = prescriptionService.getPrescription(appointmentId);
    Prescription prescription = (Prescription) response.getBody();

    if (prescription == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Prescription not found for appointment ID: " + appointmentId);
    }

    return ResponseEntity.ok(prescription);
    }

    
// 1. Set Up the Controller Class:
//    - Annotate the class with `@RestController` to define it as a REST API controller.
//    - Use `@RequestMapping("${api.path}prescription")` to set the base path for all prescription-related endpoints.
//    - This controller manages creating and retrieving prescriptions tied to appointments.


// 2. Autowire Dependencies:
//    - Inject `PrescriptionService` to handle logic related to saving and fetching prescriptions.
//    - Inject the shared `Service` class for token validation and role-based access control.
//    - Inject `AppointmentService` to update appointment status after a prescription is issued.


// 3. Define the `savePrescription` Method:
//    - Handles HTTP POST requests to save a new prescription for a given appointment.
//    - Accepts a validated `Prescription` object in the request body and a doctor’s token as a path variable.
//    - Validates the token for the `"doctor"` role.
//    - If the token is valid, updates the status of the corresponding appointment to reflect that a prescription has been added.
//    - Delegates the saving logic to `PrescriptionService` and returns a response indicating success or failure.


// 4. Define the `getPrescription` Method:
//    - Handles HTTP GET requests to retrieve a prescription by its associated appointment ID.
//    - Accepts the appointment ID and a doctor’s token as path variables.
//    - Validates the token for the `"doctor"` role using the shared service.
//    - If the token is valid, fetches the prescription using the `PrescriptionService`.
//    - Returns the prescription details or an appropriate error message if validation fails.


}
