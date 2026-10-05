package com.insuradrive.repository;

import com.insuradrive.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    @Query("SELECT a FROM AuditLog a ORDER BY a.activityDateTime DESC")
    List<AuditLog> findAllByOrderByTimestampDesc();
}
