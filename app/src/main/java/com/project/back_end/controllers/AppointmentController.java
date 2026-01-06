package com.project.back_end.controllers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.MvcService;
import com.project.back_end.services.TokenService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;
    private final MvcService service;
    private final TokenService tokenService;

    public AppointmentController(AppointmentService appointmentService, MvcService service, TokenService tokenService) {
        this.appointmentService = appointmentService;
        this.service = service;
        this.tokenService = tokenService;
    }

    @GetMapping("/{appointmentDate}/{patientName}/{token}")
    public ResponseEntity<?> getAppointments(
            @PathVariable String appointmentDate,
            @PathVariable String patientName,
            @PathVariable String token) {

        // 1. Token validieren (Rolle "doctor")
        String error = service.validateToken(token, "doctor");
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        // 2. doctorId aus dem Token extrahieren
        Long doctorId;
        try {
            // Extrahiere Claims aus dem Token
            Map<String, String> claims = tokenService.extractClaims(token);
            String doctorIdStr = claims.get("doctorId"); // Annahme: doctorId ist als Claim im Token

            if (doctorIdStr == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: doctorId missing.");
            }

            doctorId = Long.parseLong(doctorIdStr); // Umwandlung String → Long

        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid doctorId in token.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to extract doctorId.");
        }

        // 3. Datum parsen
        LocalDate date;
        try {
            date = LocalDate.parse(appointmentDate);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid date format. Use YYYY-MM-DD.");
        }

        // 4. Appointments abrufen (mit doctorId statt getUserIdFromToken)
        return ResponseEntity.ok(
                appointmentService.getAppointments(
                        doctorId,  // Hier: doctorId statt service.getUserIdFromToken(token)
                        patientName,
                        date
                )
        );
    }


    @PostMapping("/book/{token}")
    public ResponseEntity<?> bookAppointment(@Valid @RequestBody Appointment appointment,
                                            @PathVariable String token) {
        // 1. Token validieren
        String error = service.validateToken(token, "patient");
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        // 2. Daten extrahieren
        Long doctorId = appointment.getDoctorId();
        if (doctorId == null) {
            return ResponseEntity.badRequest().body("Doctor ID is required.");
        }

        LocalDate appointmentDate = appointment.getAppointmentTime().toLocalDate();
        LocalTime appointmentTime = appointment.getAppointmentTime().toLocalTime();

        // 3. Validierung (mit int-Rückgabe)
        int validationResult = service.validateAppointment(doctorId, appointmentDate, appointmentTime);

        // 4. Ergebnis der Validierung prüfen
        if (validationResult == -1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Doctor not found.");
        } else if (validationResult == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Doctor not available at the selected time.");
        } else if (validationResult != 1) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Validation failed.");
        }

        // 5. Termin buchen
        int result = appointmentService.bookAppointment(appointment);
        if (result == 1) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Appointment booked successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to book appointment.");
        }
    }


    @PutMapping("/update/{token}")
    public ResponseEntity<String> updateAppointment(
            @Valid @RequestBody Appointment updatedAppointment,
            @PathVariable String token) {

        // 1. Token validieren und patientId extrahieren
        String tokenError = service.validateToken(token, "patient");
        if (!tokenError.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(tokenError);
        }
        Long patientId = service.getUserIdFromToken(token);

        // 2. Appointment-ID prüfen
        Long appointmentId = updatedAppointment.getId();
        if (appointmentId == null) {
            return ResponseEntity.badRequest().body("Appointment ID is required.");
        }

        // 3. Update im Service durchführen
        String updateResult = appointmentService.updateAppointment(
                appointmentId,
                patientId,
                updatedAppointment
        );

        // 4. Ergebnis verarbeiten
        switch (updateResult) {
            case "Appointment updated successfully.":
                return ResponseEntity.ok(updateResult);
            case "Appointment not found.":
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(updateResult);
            case "Unauthorized: You can only update your own appointments.":
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(updateResult);
            case "Cannot update appointment: Appointment is not in a modifiable state.":
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(updateResult);
            case "Doctor is not available at the selected time.":
                return ResponseEntity.status(HttpStatus.CONFLICT).body(updateResult);
            default:
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to update appointment.");
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
