package com.clinicflow.repository.tenant;

import com.clinicflow.entity.tenant.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    Optional<Patient> findByPhone(String phone);
    Optional<Patient> findByUhid(String uhid);

    // Search by name or phone — used by receptionist search bar
    @Query("SELECT p FROM Patient p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')) OR p.phone LIKE CONCAT('%', :q, '%')")
    List<Patient> search(String q);

    // Next UHID number for this clinic — atomic, concurrency-safe.
    // Uses a per-schema Postgres sequence (see V2 tenant migration) instead of
    // COUNT(*), which races under concurrent registrations and recycles ids
    // after deletes.
    @Query(value = "SELECT nextval('patient_uhid_seq')", nativeQuery = true)
    long nextUhidSeq();
}
