package com.iris.assistant.api;

import com.iris.assistant.domain.Appliance;
import com.iris.assistant.domain.InteractionResult;
import com.iris.assistant.domain.UserProfile;
import com.iris.assistant.service.ApplianceService;
import com.iris.assistant.service.IrisOrchestrator;
import com.iris.assistant.service.UserDirectory;
import java.util.Collection;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class IrisController {

    private final IrisOrchestrator irisOrchestrator;
    private final UserDirectory userDirectory;
    private final ApplianceService applianceService;

    public IrisController(
            IrisOrchestrator irisOrchestrator,
            UserDirectory userDirectory,
            ApplianceService applianceService
    ) {
        this.irisOrchestrator = irisOrchestrator;
        this.userDirectory = userDirectory;
        this.applianceService = applianceService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "assistant", "Iris");
    }

    @GetMapping("/users")
    public Collection<UserProfile> users() {
        return userDirectory.findAll();
    }

    @GetMapping("/appliances")
    public Collection<Appliance> appliances() {
        return applianceService.findAll();
    }

    @PostMapping("/interactions")
    public InteractionResult interact(@RequestBody InteractionRequest request) {
        return irisOrchestrator.handle(request);
    }

    @PutMapping("/appliances/{id}")
    public ResponseEntity<Appliance> updateAppliance(
            @PathVariable String id,
            @RequestBody ApplianceUpdateRequest request
    ) {
        return applianceService.updateStatus(id, request.status())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/demo/reset")
    public Collection<Appliance> resetDemo() {
        applianceService.reset();
        return applianceService.findAll();
    }
}
