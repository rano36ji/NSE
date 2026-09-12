package com.rd.nse.service;

import com.rd.nse.entity.Interval;
import com.rd.nse.repository.IntervalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class IntervalService {

    @Autowired
    private IntervalRepository intervalRepository;

    public List<Interval> getAllIntervals() {
        return intervalRepository.findAll();
    }

    public Interval addInterval(Interval interval) {
        return intervalRepository.save(interval);
    }

    public Optional<Interval> getByIntervalCode(String code) {
        return intervalRepository.findByIntervalCode(code);
    }

    public Optional<Interval> getById(Long id) {
        return intervalRepository.findById(id);
    }
}
