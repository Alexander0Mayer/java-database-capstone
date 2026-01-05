package com.project.back_end.services;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;


@Service
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public DoctorService(DoctorRepository doctorRepository,
                         AppointmentRepository appointmentRepository,
                         TokenService tokenService,
                         PasswordEncoder passwordEncoder) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }
    @Transactional
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndDate(doctorId, date);
        java.util.Set<LocalTime> bookedSlots = appointments.stream()
                .map(appointment -> appointment.getAppointmentTime().toLocalTime())
                .collect(Collectors.toSet());

        java.util.List<String> allSlots = java.util.stream.IntStream.range(9, 17)
                .mapToObj(hour -> String.format("%02d:00", hour))
                .collect(Collectors.toList());

        return allSlots.stream()
                .filter(slot -> !bookedSlots.contains(java.time.LocalTime.parse(slot)))
                .collect(Collectors.toList());
    }
    @Transactional
    public int saveDoctor(Doctor doctor) {
        try {
            if (doctorRepository.findByEmail(doctor.getEmail()) != null) {
                return -1; // Conflict
            }
            // Passwort hashen, bevor es gespeichert wird
            doctor.setPassword(passwordEncoder.encode(doctor.getPassword()));
            doctorRepository.save(doctor);
            return 1; // Success
        } catch (Exception e) {
            return 0; // Internal Error
        }
    }
    @Transactional
    public int updateDoctor(Doctor doctor) {
        try {
            if (!doctorRepository.findById(doctor.getId()).isPresent()) {
                return -1; // Doctor not found
            }
            doctorRepository.save(doctor);
            return 1; // Success
        } catch (Exception e) {
            return 0; // Internal Error
        }
    }
    @Transactional
    public java.util.List<Doctor> getDoctors() {
        return doctorRepository.findAll();
    }
    @Transactional
    public int deleteDoctor(Long doctorId) {
        try {
            if (!doctorRepository.findById(doctorId).isPresent()) {
                return -1; // Doctor not found
            }
            appointmentRepository.deleteAllByDoctorId(doctorId);
            doctorRepository.deleteById(doctorId);
            return 1; // Success
        } catch (Exception e) {
            return 0; // Internal Error
        }
    }
    @Transactional(readOnly = true)
    public Map<String, Object> validateDoctor(String email, String password) {
        Map<String, Object> response = new HashMap<>();

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            response.put("status", "error");
            response.put("message", "E-Mail und Passwort sind Pflichtfelder.");
            return response;
        }

        try {
            Doctor doctorOpt = doctorRepository.findByEmail(email);
            if (doctorOpt == null) {
                response.put("status", "error");
                response.put("message", "Arzt nicht gefunden.");
                return response;
            }

            Doctor doctor = doctorOpt;

            if (!passwordEncoder.matches(password, doctor.getPassword())) {
                response.put("status", "error");
                response.put("message", "wrong password.");
                return response;
            }

            // doctor.getId() ist ein primitiver long-Wert und kann nicht null sein!
            Map<String, String> claims = new HashMap<>();
            claims.put("role", "DOCTOR");
            claims.put("doctorId", String.valueOf(doctor.getId()));  // Sicher, da long nie null ist

            String token = tokenService.generateToken(email, claims);

            response.put("status", "success");
            response.put("token", token);
            response.put("doctorId", doctor.getId());

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "internal server error.");
        }

        return response;
    }


    @Transactional
    public java.util.List<Doctor> findDoctorByName(String name) {
        return doctorRepository.findByNameContainingIgnoreCase(name);
    }
    @Transactional
    public java.util.List<Doctor> filterDoctorsByNameSpecilityandTime(String name, String specialty, String time) {
        java.util.List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyContainingIgnoreCase(name, specialty);
        return filterDoctorByTime(doctors, time);
    }
    @Transactional
    public List<Doctor> filterDoctorByTime(List<Doctor> doctors, String time) {

        return doctors.stream()
            .filter(doctor ->
                doctor.getAvailableTimes().stream().anyMatch(slot -> {
                    // Slot-Format: "09:00-10:00"
                    String startTimeStr = slot.split("-")[0];
                    LocalTime startTime = LocalTime.parse(startTimeStr);

                    int hour = startTime.getHour();

                    if ("AM".equalsIgnoreCase(time)) {
                        return hour >= 9 && hour < 12;
                    } else if ("PM".equalsIgnoreCase(time)) {
                        return hour >= 12 && hour < 17;
                    }
                    return false;
                })
            )
            .collect(Collectors.toList());
    }

    @Transactional
    public java.util.List<Doctor> filterDoctorByNameAndTime(String name, String time) {
        java.util.List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCase(name);
        return filterDoctorByTime(doctors, time);
    }
    @Transactional
    public java.util.List<Doctor> filterDoctorByNameAndSpecility(String name, String specialty) {
        return doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyContainingIgnoreCase(name, specialty);
    }
    @Transactional
    public java.util.List<Doctor> filterDoctorByTimeAndSpecility(String specialty, String time) {
        java.util.List<Doctor> doctors = doctorRepository.findBySpecialtyContainingIgnoreCase(specialty);
        return filterDoctorByTime(doctors, time);
    }
    @Transactional
    public java.util.List<Doctor> filterDoctorBySpecility(String specialty) {
        return doctorRepository.findBySpecialtyContainingIgnoreCase(specialty);
    }   
    @Transactional
    public java.util.List<Doctor> filterDoctorsByTime(String time) {
        java.util.List<Doctor> doctors = doctorRepository.findAll();
        return filterDoctorByTime(doctors, time);
    }   

    List<Doctor> filterByNameSpecialtyAndTimeSlot(String name, String specialty, String timeSlot) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    public List<Doctor> filterByNameAndSpecialty(String name, String specialty) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'filterByNameAndSpecialty'");
    }
    public List<Doctor> filterByNameAndTimeSlot(String name, String timeSlot) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'filterByNameAndTimeSlot'");
    }
    public List<Doctor> filterByName(String name) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'filterByName'");
    }
    public List<Doctor> filterBySpecialty(String specialty) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'filterBySpecialty'");
    }
    public List<Doctor> filterByTimeSlot(String timeSlot) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'filterByTimeSlot'");
    }
}

// 1. **Add @Service Annotation**:
//    - This class should be annotated with `@Service` to indicate that it is a service layer class.
//    - The `@Service` annotation marks this class as a Spring-managed bean for business logic.
//    - Instruction: Add `@Service` above the class declaration.

// 2. **Constructor Injection for Dependencies**:
//    - The `DoctorService` class depends on `DoctorRepository`, `AppointmentRepository`, and `TokenService`.
//    - These dependencies should be injected via the constructor for proper dependency management.
//    - Instruction: Ensure constructor injection is used for injecting dependencies into the service.

// 3. **Add @Transactional Annotation for Methods that Modify or Fetch Database Data**:
//    - Methods like `getDoctorAvailability`, `getDoctors`, `findDoctorByName`, `filterDoctorsBy*` should be annotated with `@Transactional`.
//    - The `@Transactional` annotation ensures that database operations are consistent and wrapped in a single transaction.
//    - Instruction: Add the `@Transactional` annotation above the methods that perform database operations or queries.

// 4. **getDoctorAvailability Method**:
//    - Retrieves the available time slots for a specific doctor on a particular date and filters out already booked slots.
//    - The method fetches all appointments for the doctor on the given date and calculates the availability by comparing against booked slots.
//    - Instruction: Ensure that the time slots are properly formatted and the available slots are correctly filtered.

// 5. **saveDoctor Method**:
//    - Used to save a new doctor record in the database after checking if a doctor with the same email already exists.
//    - If a doctor with the same email is found, it returns `-1` to indicate conflict; `1` for success, and `0` for internal errors.
//    - Instruction: Ensure that the method correctly handles conflicts and exceptions when saving a doctor.

// 6. **updateDoctor Method**:
//    - Updates an existing doctor's details in the database. If the doctor doesn't exist, it returns `-1`.
//    - Instruction: Make sure that the doctor exists before attempting to save the updated record and handle any errors properly.

// 7. **getDoctors Method**:
//    - Fetches all doctors from the database. It is marked with `@Transactional` to ensure that the collection is properly loaded.
//    - Instruction: Ensure that the collection is eagerly loaded, especially if dealing with lazy-loaded relationships (e.g., available times). 

// 8. **deleteDoctor Method**:
//    - Deletes a doctor from the system along with all appointments associated with that doctor.
//    - It first checks if the doctor exists. If not, it returns `-1`; otherwise, it deletes the doctor and their appointments.
//    - Instruction: Ensure the doctor and their appointments are deleted properly, with error handling for internal issues.

// 9. **validateDoctor Method**:
//    - Validates a doctor's login by checking if the email and password match an existing doctor record.
//    - It generates a token for the doctor if the login is successful, otherwise returns an error message.
//    - Instruction: Make sure to handle invalid login attempts and password mismatches properly with error responses.

// 10. **findDoctorByName Method**:
//    - Finds doctors based on partial name matching and returns the list of doctors with their available times.
//    - This method is annotated with `@Transactional` to ensure that the database query and data retrieval are properly managed within a transaction.
//    - Instruction: Ensure that available times are eagerly loaded for the doctors.


// 11. **filterDoctorsByNameSpecilityandTime Method**:
//    - Filters doctors based on their name, specialty, and availability during a specific time (AM/PM).
//    - The method fetches doctors matching the name and specialty criteria, then filters them based on their availability during the specified time period.
//    - Instruction: Ensure proper filtering based on both the name and specialty as well as the specified time period.

// 12. **filterDoctorByTime Method**:
//    - Filters a list of doctors based on whether their available times match the specified time period (AM/PM).
//    - This method processes a list of doctors and their available times to return those that fit the time criteria.
//    - Instruction: Ensure that the time filtering logic correctly handles both AM and PM time slots and edge cases.


// 13. **filterDoctorByNameAndTime Method**:
//    - Filters doctors based on their name and the specified time period (AM/PM).
//    - Fetches doctors based on partial name matching and filters the results to include only those available during the specified time period.
//    - Instruction: Ensure that the method correctly filters doctors based on the given name and time of day (AM/PM).

// 14. **filterDoctorByNameAndSpecility Method**:
//    - Filters doctors by name and specialty.
//    - It ensures that the resulting list of doctors matches both the name (case-insensitive) and the specified specialty.
//    - Instruction: Ensure that both name and specialty are considered when filtering doctors.


// 15. **filterDoctorByTimeAndSpecility Method**:
//    - Filters doctors based on their specialty and availability during a specific time period (AM/PM).
//    - Fetches doctors based on the specified specialty and filters them based on their available time slots for AM/PM.
//    - Instruction: Ensure the time filtering is accurately applied based on the given specialty and time period (AM/PM).

// 16. **filterDoctorBySpecility Method**:
//    - Filters doctors based on their specialty.
//    - This method fetches all doctors matching the specified specialty and returns them.
//    - Instruction: Make sure the filtering logic works for case-insensitive specialty matching.

// 17. **filterDoctorsByTime Method**:
//    - Filters all doctors based on their availability during a specific time period (AM/PM).
//    - The method checks all doctors' available times and returns those available during the specified time period.
//    - Instruction: Ensure proper filtering logic to handle AM/PM time periods.

