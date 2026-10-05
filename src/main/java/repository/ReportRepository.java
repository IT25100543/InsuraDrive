package com.insuradrive.repository;

import com.insuradrive.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findAllByOrderByGeneratedDateDesc();

    @Query("SELECT r FROM Report r WHERE r.staff.userID = :staffId ORDER BY r.generatedDate DESC")
    List<Report> findByStaffId(@Param("staffId") Long staffId);

    List<Report> findByFormat(String format);
}
