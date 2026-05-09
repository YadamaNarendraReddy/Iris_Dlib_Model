package com.iris.assistant.service;

import com.iris.assistant.domain.Appliance;
import com.iris.assistant.domain.ApplianceStatus;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ApplianceService {

    private final Map<String, Appliance> appliances = new LinkedHashMap<>();

    public ApplianceService() {
        reset();
    }

    public Collection<Appliance> findAll() {
        return appliances.values();
    }

    public Optional<Appliance> findById(String id) {
        return Optional.ofNullable(appliances.get(id));
    }

    public Optional<Appliance> updateStatus(String id, ApplianceStatus status) {
        if (status == null || !appliances.containsKey(id)) {
            return Optional.empty();
        }
        Appliance updated = appliances.get(id).withStatus(status);
        appliances.put(id, updated);
        return Optional.of(updated);
    }

    public void reset() {
        appliances.clear();
        appliances.put("living-room-lights",
                new Appliance("living-room-lights", "Living room lights", "Living Room", ApplianceStatus.OFF));
    }
}
