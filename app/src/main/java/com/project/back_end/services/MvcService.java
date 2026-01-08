package com.project.back_end.services;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;




@Service
public class MvcService {
    public MvcService(TokenService tokenService,
                      AdminRepository adminRepository,
                      DoctorRepository doctorRepository,
                      PatientRepository patientRepository,
                      DoctorService doctorService,
                      PatientService patientService,
                      AppointmentRepository appointmentRepository) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.appointmentRepository = appointmentRepository;
    }
    public String validateToken(String token, String userRole) {
        boolean isValid = tokenService.validateToken(token, userRole);
        if (!isValid) {
            return "Unauthorized: Invalid or expired token.";
        }
        return "";
    }
    public Map<String, Object> validateAdmin(String username, String password) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Admin> adminOpt = adminRepository.findByUsername(username);

            if (adminOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Admin not found.");
                return response;
            }

            Admin admin = adminOpt.get();

            if (!admin.getPassword().equals(password)) {
                response.put("success", false);
                response.put("message", "Incorrect password.");
                return response;
            }

            // Erfolg: Token generieren
            String token = tokenService.generateToken(admin.getUsername());

            response.put("success", true);
            response.put("message", "Login successful.");
            response.put("token", token);

            // Optional: Claims hinzufügen (falls benötigt)
            Map<String, String> claims = tokenService.extractClaims(token);
            response.put("claims", claims);

            return response;

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return response;
        }
    }

    public List<Doctor> filterDoctor(String name, String specialty, String timeSlot) {
        if (name != null && specialty != null && timeSlot != null) {
            return doctorService.filterByNameSpecialtyAndTimeSlot(name, specialty, timeSlot);
        } else if (name != null && specialty != null) {
            return doctorService.filterByNameAndSpecialty(name, specialty);
        } else if (name != null && timeSlot != null) {
            return doctorService.filterByNameAndTimeSlot(name, timeSlot);
        } else if (specialty != null && timeSlot != null) {
            return doctorService.filterByNameAndTimeSlot(specialty, timeSlot);
        } else if (name != null) {
            return doctorService.filterByName(name);
        } else if (specialty != null) {
            return doctorService.filterBySpecialty(specialty);
        } else if (timeSlot != null) {
            return doctorService.filterByTimeSlot(timeSlot);
        } else {
            return doctorRepository.findAll();
        }
    }
    public int validateAppointment(long doctorId, LocalDate appointmentDate, LocalTime appointmentTime) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isPresent()) {
            List<String> availableSlots = doctorService.getDoctorAvailability(doctorId, appointmentDate);
            for (String slot : availableSlots) {
                if (slot.equals(appointmentTime)) {
                    return 1; // Valid appointment time
                }
            }
            return 0; // Invalid appointment time
        } else {
            return -1; // Doctor does not exist
        }
    }
    public boolean validatePatient(String email, String phoneNumber) {
        Optional<Patient> patientByEmail = patientRepository.findByEmail(email);
        Optional<Patient> patientByPhone = patientRepository.findByPhone(phoneNumber);
        return patientByEmail.isEmpty() && patientByPhone.isEmpty();
    }
    public ResponseEntity<?> validatePatientLogin(String email, String password) {
        try {
            Optional<Patient> patientOpt = patientRepository.findByEmail(email);
            if (patientOpt.isPresent()) {
                Patient patient = patientOpt.get();
                if (patient.getPassword().equals(password)) {
                    String token = tokenService.generateToken(patient.getEmail());
                    return ResponseEntity.ok().body(Map.of("token", token));
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body("Unauthorized: Incorrect password.");
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Unauthorized: Patient not found.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error: " + e.getMessage());
        }
    }
    public List<Appointment> filterPatient(String token, String condition, String doctorName) {
        String email = tokenService.extractEmail(token);
        if (condition != null && doctorName != null) {
            return patientService.filterAppointmentsByConditionAndDoctor(email, condition, doctorName);
        } else if (condition != null) {
            return patientService.filterAppointmentsByCondition(email, condition);
        } else if (doctorName != null) {
            return patientService.filterAppointmentsByDoctor(email, doctorName);
        } else {
            return patientService.getAllAppointments(email);
        }
    }
    
    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;
    private final AppointmentRepository appointmentRepository;


    public boolean isDoctorAvailable(Long doctorId, LocalDateTime startTime, LocalDateTime endTime, long excludeAppointmentId) {
    // Suche nach Terminen des Arztes im gewünschten Zeitfenster, außer dem aktuellen Termin
    List<Appointment> conflictingAppointments = appointmentRepository
        .findByDoctor_IdAndAppointmentTimeBetween(doctorId, startTime, endTime)
        .stream()
        .filter(appointment -> appointment.getId() != excludeAppointmentId)
        .collect(Collectors.toList());

    // Wenn es keine Konflikte gibt, ist der Arzt verfügbar
    return conflictingAppointments.isEmpty();
}
// 1. **@Service Annotation**
// The @Service annotation marks this class as a service component in Spring. This allows Spring to automatically detect it through component scanning
// and manage its lifecycle, enabling it to be injected into controllers or other services using @Autowired or constructor injection.
    public Long getUserIdFromToken(String token) {
        return Long.parseLong(tokenService.extractClaims(token).get("userId"));
    }

// 2. **Constructor Injection for Dependencies**
// The constructor injects all required dependencies (TokenService, Repositories, and other Services). This approach promotes loose coupling, improves testability,
// and ensures that all required dependencies are provided at object creation time.

// 3. **validateToken Method**
// This method checks if the provided JWT token is valid for a specific user. It uses the TokenService to perform the validation.
// If the token is invalid or expired, it returns a 401 Unauthorized response with an appropriate error message. This ensures security by preventing
// unauthorized access to protected resources.

// 4. **validateAdmin Method**
// This method validates the login credentials for an admin user.
// - It first searches the admin repository using the provided username.
// - If an admin is found, it checks if the password matches.
// - If the password is correct, it generates and returns a JWT token (using the admin’s username) with a 200 OK status.
// - If the password is incorrect, it returns a 401 Unauthorized status with an error message.
// - If no admin is found, it also returns a 401 Unauthorized.
// - If any unexpected error occurs during the process, a 500 Internal Server Error response is returned.
// This method ensures that only valid admin users can access secured parts of the system.

// 5. **filterDoctor Method**
// This method provides filtering functionality for doctors based on name, specialty, and available time slots.
// - It supports various combinations of the three filters.
// - If none of the filters are provided, it returns all available doctors.
// This flexible filtering mechanism allows the frontend or consumers of the API to search and narrow down doctors based on user criteria.

// 6. **validateAppointment Method**
// This method validates if the requested appointment time for a doctor is available.
// - It first checks if the doctor exists in the repository.
// - Then, it retrieves the list of available time slots for the doctor on the specified date.
// - It compares the requested appointment time with the start times of these slots.
// - If a match is found, it returns 1 (valid appointment time).
// - If no matching time slot is found, it returns 0 (invalid).
// - If the doctor doesn’t exist, it returns -1.
// This logic prevents overlapping or invalid appointment bookings.

// 7. **validatePatient Method**
// This method checks whether a patient with the same email or phone number already exists in the system.
// - If a match is found, it returns false (indicating the patient is not valid for new registration).
// - If no match is found, it returns true.
// This helps enforce uniqueness constraints on patient records and prevent duplicate entries.

// 8. **validatePatientLogin Method**
// This method handles login validation for patient users.
// - It looks up the patient by email.
// - If found, it checks whether the provided password matches the stored one.
// - On successful validation, it generates a JWT token and returns it with a 200 OK status.
// - If the password is incorrect or the patient doesn't exist, it returns a 401 Unauthorized with a relevant error.
// - If an exception occurs, it returns a 500 Internal Server Error.
// This method ensures only legitimate patients can log in and access their data securely.

// 9. **filterPatient Method**
// This method filters a patient's appointment history based on condition and doctor name.
// - It extracts the email from the JWT token to identify the patient.
// - Depending on which filters (condition, doctor name) are provided, it delegates the filtering logic to PatientService.
// - If no filters are provided, it retrieves all appointments for the patient.
// This flexible method supports patient-specific querying and enhances user experience on the client side.

    public Map<String, String> extractClaims(String token) {
        try {
            return tokenService.extractClaims(token); // Delegiert an TokenService
        } catch (Exception e) {
            return null;
        }
    }
    public Patient getPatientFromToken(String token) {
    try {
        // 1. Extrahiere die E-Mail aus dem Token (angenommen, der Token enthält die E-Mail als Claim)
        String email = tokenService.extractEmail(token);

        // 2. Suche den Patienten in der Datenbank
        Optional<Patient> patientOpt = patientRepository.findByEmail(email);

        // 3. Falls nicht gefunden, werfe eine Exception
        if (patientOpt.isEmpty()) {
            throw new RuntimeException("Patient not found for token: " + token);
        }

        // 4. Gib den Patienten zurück
        return patientOpt.get();

    } catch (Exception e) {
        throw new RuntimeException("Failed to retrieve patient from token: " + e.getMessage(), e);
    }
}


}
