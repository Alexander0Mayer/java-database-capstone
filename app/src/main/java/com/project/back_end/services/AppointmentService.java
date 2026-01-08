package com.project.back_end.services;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;




@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final MvcService service;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;


    public AppointmentService(AppointmentRepository appointmentRepository, MvcService service,
                              TokenService tokenService, PatientRepository patientRepository,
                              DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.service = service;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional
    public int bookAppointment(Appointment appointment) {
        if (appointment == null) {
            throw new IllegalArgumentException("Appointment object must not be null");
        }
        try {
            appointmentRepository.save(appointment);
            return 1; // Success
        } catch (Exception e) {
            return 0; // Failure
        }
    }

    @Transactional
    public String updateAppointment(Long appointmentId, Long patientId, Appointment updatedAppointment) {
        Appointment existingAppointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (existingAppointment == null) {
            return "Appointment not found.";
        }
        if (!existingAppointment.getPatientId().equals(patientId)) {
            return "Unauthorized: You can only update your own appointments.";
        }
        if (existingAppointment.getStatus() != 0) {
            return "Cannot update appointment: Appointment is not in a modifiable state.";
        }
        boolean isDoctorAvailable = service.isDoctorAvailable(
                updatedAppointment.getDoctorId(),
                updatedAppointment.getAppointmentTime(),
                updatedAppointment.getAppointmentTime().plusHours(1),
                appointmentId
        );
        if (!isDoctorAvailable) {
            return "Doctor is not available at the selected time.";
        }
        existingAppointment.setDoctorId(updatedAppointment.getDoctorId());
        existingAppointment.setAppointmentTime(updatedAppointment.getAppointmentTime());
        appointmentRepository.save(existingAppointment);
        return "Appointment updated successfully.";
    }
    @Transactional
    public String cancelAppointment(Long appointmentId, Long patientId) {
        Appointment existingAppointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (existingAppointment == null) {
            return "Appointment not found.";
        }
        if (!existingAppointment.getPatientId().equals(patientId)) {
            return "Unauthorized: You can only cancel your own appointments.";
        }
        try { 
            appointmentRepository.deleteById(appointmentId);
            return "Appointment canceled successfully.";
        } catch (Exception e) {
            return "Error occurred while canceling the appointment.";
        }
    }
    @Transactional
    public List<AppointmentDTO> getAppointments(Long doctorId, String patientName, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        List<Appointment> appointments;
        if (doctorId == null || date == null) {
                throw new IllegalArgumentException("Doctor ID and date must not be null");
            }
        if (patientName == null || patientName.isEmpty()) {
            appointments = appointmentRepository.findByDoctor_IdAndAppointmentTimeBetween(doctorId, startOfDay, endOfDay);
        } else {
            appointments = appointmentRepository.findByDoctor_IdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(doctorId, patientName, startOfDay, endOfDay);
        }
        return appointments.stream().map(appointment -> {
            Patient patient = patientRepository.findById(appointment.getPatientId()).orElse(null);
            Doctor doctor = doctorRepository.findById(appointment.getDoctorId()).orElse(null);
            return new AppointmentDTO(
                    appointment.getId(),
                    appointment.getDoctorId(),
                    doctor != null ? doctor.getName() : "Unknown",
                    appointment.getPatientId(),
                    patient != null ? patient.getName() : "Unknown",
                    patient != null ? patient.getEmail() : "Unknown",
                    patient != null ? patient.getPhone() : "Unknown",
                    patient != null ? patient.getAddress() : "Unknown",
                    appointment.getAppointmentTime(),
                    appointment.getStatus()
            );
        }).collect(Collectors.toList());
    }
    @Transactional
    public void changeStatus(Long appointmentId, int status) {
        appointmentRepository.updateStatus(status, appointmentId);
    }

    public Optional<Appointment> getAppointmentById(Long appointmentId) {
        return appointmentRepository.findById(appointmentId);
    }


    public List<Appointment> getAppointmentsByDoctorAndDate(Long doctorId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay(); // 00:00
        LocalDateTime endOfDay = date.atTime(23, 59, 59); // 23:59:59

        return appointmentRepository.findByDoctorIdAndDate(doctorId, startOfDay, endOfDay);
    }


// 1. **Add @Service Annotation**:
//    - To indicate that this class is a service layer class for handling business logic.
//    - The `@Service` annotation should be added before the class declaration to mark it as a Spring service component.
//    - Instruction: Add `@Service` above the class definition.

// 2. **Constructor Injection for Dependencies**:
//    - The `AppointmentService` class requires several dependencies like `AppointmentRepository`, `Service`, `TokenService`, `PatientRepository`, and `DoctorRepository`.
//    - These dependencies should be injected through the constructor.
//    - Instruction: Ensure constructor injection is used for proper dependency management in Spring.

// 3. **Add @Transactional Annotation for Methods that Modify Database**:
//    - The methods that modify or update the database should be annotated with `@Transactional` to ensure atomicity and consistency of the operations.
//    - Instruction: Add the `@Transactional` annotation above methods that interact with the database, especially those modifying data.

// 4. **Book Appointment Method**:
//    - Responsible for saving the new appointment to the database.
//    - If the save operation fails, it returns `0`; otherwise, it returns `1`.
//    - Instruction: Ensure that the method handles any exceptions and returns an appropriate result code.

// 5. **Update Appointment Method**:
//    - This method is used to update an existing appointment based on its ID.
//    - It validates whether the patient ID matches, checks if the appointment is available for updating, and ensures that the doctor is available at the specified time.
//    - If the update is successful, it saves the appointment; otherwise, it returns an appropriate error message.
//    - Instruction: Ensure proper validation and error handling is included for appointment updates.

// 6. **Cancel Appointment Method**:
//    - This method cancels an appointment by deleting it from the database.
//    - It ensures the patient who owns the appointment is trying to cancel it and handles possible errors.
//    - Instruction: Make sure that the method checks for the patient ID match before deleting the appointment.

// 7. **Get Appointments Method**:
//    - This method retrieves a list of appointments for a specific doctor on a particular day, optionally filtered by the patient's name.
//    - It uses `@Transactional` to ensure that database operations are consistent and handled in a single transaction.
//    - Instruction: Ensure the correct use of transaction boundaries, especially when querying the database for appointments.

// 8. **Change Status Method**:
//    - This method updates the status of an appointment by changing its value in the database.
//    - It should be annotated with `@Transactional` to ensure the operation is executed in a single transaction.
//    - Instruction: Add `@Transactional` before this method to ensure atomicity when updating appointment status.


}
