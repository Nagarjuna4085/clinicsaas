package com.clinicflow.repository.tenant;

import com.clinicflow.entity.tenant.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {
    Optional<Prescription> findByAppointmentId(UUID appointmentId);
}
