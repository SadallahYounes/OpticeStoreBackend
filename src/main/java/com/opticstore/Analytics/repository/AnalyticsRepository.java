package com.opticstore.Analytics.repository;

import com.opticstore.Analytics.model.AnalyticsData;
import com.opticstore.Analytics.model.TimePeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnalyticsRepository extends JpaRepository<AnalyticsData, Long> {

    Optional<AnalyticsData> findBySnapshotDateAndPeriod(LocalDateTime snapshotDate, TimePeriod period);

    List<AnalyticsData> findBySnapshotDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT a FROM AnalyticsData a WHERE a.period = :period ORDER BY a.snapshotDate DESC")
    List<AnalyticsData> findLatestByPeriod(@Param("period") TimePeriod period, org.springframework.data.domain.Pageable pageable);

    @Query(value = """
        SELECT * FROM analytics_snapshots 
        WHERE snapshot_date >= :startDate 
        ORDER BY snapshot_date DESC 
        LIMIT :limit
        """, nativeQuery = true)
    List<AnalyticsData> findRecentSnapshots(@Param("startDate") LocalDateTime startDate, @Param("limit") int limit);
}