package com.rd.nse.controller;

import com.rd.nse.entity.Interval;
import com.rd.nse.service.IntervalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/intervals")
public class IntervalController {

    @Autowired
    private IntervalService intervalService;

    @GetMapping
    public List<Interval> getAll() {
        return intervalService.getAllIntervals();
    }

    @PostMapping
    public Interval create(@RequestBody Interval interval) {
        return intervalService.addInterval(interval);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Interval> getById(@PathVariable Long id) {
        return intervalService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Interval> getByCode(@PathVariable String code) {
        return intervalService.getByIntervalCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
