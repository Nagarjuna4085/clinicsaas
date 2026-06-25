package com.clinicflow.repository.global;

import com.clinicflow.entity.global.StaffDirectory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StaffDirectoryRepository extends JpaRepository<StaffDirectory, String> {
    Optional<StaffDirectory> findByPhone(String phone);
}
