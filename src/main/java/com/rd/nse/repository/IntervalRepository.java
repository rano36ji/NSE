package com.rd.nse.repository;

import com.rd.nse.entity.Interval;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IntervalRepository extends JpaRepository<Interval, Long> {
    Optional<Interval> findByIntervalCode(String intervalCode);
}
