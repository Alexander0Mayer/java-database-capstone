package com.project.back_end.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import javax.validation.Valid;
import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.MvcService;
import java.time.LocalDate;


@RestController
@RequestMapping("/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;
    private final MvcService service;

    public AppointmentController(AppointmentService appointmentService, MvcService service) {
        this.appointmentService = appointmentService;
        this.service = service;
    }

    @GetMapping("/{appointmentDate}/{patientName}/{token}")
    public ResponseEntity<?> getAppointments(@PathVariable String appointmentDate,
                                             @PathVariable String patientName,
                                             @PathVariable String token) {
        String error = service.validateToken(token, "doctor");
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        LocalDate date = LocalDate.parse(appointmentDate);
        return ResponseEntity.ok(
                appointmentService.getAppointmentsByDateAndPatientName(date, patientName)
        );
    }

    @PostMapping("/book/{token}")
    public ResponseEntity<?> bookAppointment(@Valid @RequestBody Appointment appointment,
                                             @PathVariable String token) {
        String error = service.validateToken(token, "patient");
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        String validationError = service.validateAppointment(appointment);
        if (!validationError.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationError);
        }
        int result = appointmentService.bookAppointment(appointment);
        if (result == 1) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Appointment booked successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to book appointment.");
        }
    }

    @PutMapping("/update/{token}")
    public ResponseEntity<?> updateAppointment(@Valid @RequestBody Appointment appointment,
                                               @PathVariable String token) {
        String error = service.validateToken(token, "patient");
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        String updateResult = appointmentService.updateAppointment(
                appointment.getId(),
                service.getUserIdFromToken(token),
                appointment
        );
        if (updateResult.equals("Appointment updated successfully.")) {
            return ResponseEntity.ok(updateResult);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(updateResult);
        }
    }

    @DeleteMapping("/cancel/{appointmentId}/{token}")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long appointmentId,
                                               @PathVariable String token) {
        String error = service.validateToken(token, "patient");
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        String cancelResult = appointmentService.cancelAppointment(
                appointmentId,
                service.getUserIdFromToken(token)
        );
        if (cancelResult.equals("Appointment canceled successfully.")) {
            return ResponseEntity.ok(cancelResult);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(cancelResult);
        }
    }


// 1. Set Up the Controller Class:
//    - Annotate the class with `@RestController` to define it as a REST API controller.
//    - Use `@RequestMapping("/appointments")` to set a base path for all appointment-related endpoints.
//    - This centralizes all routes that deal with booking, updating, retrieving, and canceling appointments.


// 2. Autowire Dependencies:
//    - Inject `AppointmentService` for handling the business logic specific to appointments.
//    - Inject the general `Service` class, which provides shared functionality like token validation and appointment checks.


// 3. Define the `getAppointments` Method:
//    - Handles HTTP GET requests to fetch appointments based on date and patient name.
//    - Takes the appointment date, patient name, and token as path variables.
//    - First validates the token for role `"doctor"` using the `Service`.
//    - If the token is valid, returns appointments for the given patient on the specified date.
//    - If the token is invalid or expired, responds with the appropriate message and status code.


// 4. Define the `bookAppointment` Method:
//    - Handles HTTP POST requests to create a new appointment.
//    - Accepts a validated `Appointment` object in the request body and a token as a path variable.
//    - Validates the token for the `"patient"` role.
//    - Uses service logic to validate the appointment data (e.g., check for doctor availability and time conflicts).
//    - Returns success if booked, or appropriate error messages if the doctor ID is invalid or the slot is already taken.


// 5. Define the `updateAppointment` Method:
//    - Handles HTTP PUT requests to modify an existing appointment.
//    - Accepts a validated `Appointment` object and a token as input.
//    - Validates the token for `"patient"` role.
//    - Delegates the update logic to the `AppointmentService`.
//    - Returns an appropriate success or failure response based on the update result.


// 6. Define the `cancelAppointment` Method:
//    - Handles HTTP DELETE requests to cancel a specific appointment.
//    - Accepts the appointment ID and a token as path variables.
//    - Validates the token for `"patient"` role to ensure the user is authorized to cancel the appointment.
//    - Calls `AppointmentService` to handle the cancellation process and returns the result.


}
