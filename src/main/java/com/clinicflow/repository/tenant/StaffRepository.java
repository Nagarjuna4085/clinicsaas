package com.clinicflow.repository.tenant;

import com.clinicflow.entity.tenant.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StaffRepository extends JpaRepository<Staff, UUID> {
    Optional<Staff> findByPhone(String phone);
    List<Staff> findByRoleAndIsActiveTrue(String role);
    List<Staff> findByIsActiveTrue();
}
