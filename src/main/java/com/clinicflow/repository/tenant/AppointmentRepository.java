package com.clinicflow.repository.tenant;

import com.clinicflow.entity.tenant.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    // Today's queue for a doctor — ordered by token
    List<Appointment> findByVisitDateAndDoctorIdOrderByTokenNumber(
        LocalDate date, UUID doctorId);

    // All of today's appointments (for receptionist view)
    List<Appointment> findByVisitDateOrderByTokenNumber(LocalDate date);

    // Next token number for today
    @Query("SELECT COALESCE(MAX(a.tokenNumber), 0) + 1 FROM Appointment a " +
           "WHERE a.visitDate = :date AND a.doctor.id = :doctorId")
    short nextTokenNumber(LocalDate date, UUID doctorId);

    // Follow-ups due tomorrow (for scheduled WhatsApp reminder job)
    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient " +
           "WHERE a.followupDate = :date AND a.reminderSent = false")
    List<Appointment> findFollowupsDue(LocalDate date);

    // Patient's visit history
    List<Appointment> findByPatientIdOrderByVisitDateDesc(UUID patientId);
}
