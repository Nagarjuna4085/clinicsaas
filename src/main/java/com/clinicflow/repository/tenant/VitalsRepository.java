package com.clinicflow.repository.tenant;

import com.clinicflow.entity.tenant.Vitals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VitalsRepository extends JpaRepository<Vitals, UUID> {
    Optional<Vitals> findByAppointmentId(UUID appointmentId);
}
