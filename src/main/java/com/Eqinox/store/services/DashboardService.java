package com.Eqinox.store.services;

import com.Eqinox.store.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DashboardService {

    private final UserRepository userRepository;

    public DashboardService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * LIVE monthly user growth from PostgreSQL
     */
    public Map<String, Object> getMonthlyUserGrowth() {

        List<Object[]> rows = userRepository.getMonthlyUserGrowth();

        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();

        for (Object[] row : rows) {
            labels.add((String) row[0]);           // Month (Jan, Feb, ...)
            values.add(((Number) row[1]).longValue()); // Count
        }

        return Map.of(
                "labels", labels,
                "values", values
        );
    }
}
