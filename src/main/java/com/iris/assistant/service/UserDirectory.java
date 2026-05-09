package com.iris.assistant.service;

import com.iris.assistant.domain.UserProfile;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class UserDirectory {

    private final Map<String, UserProfile> users = new LinkedHashMap<>();

    public UserDirectory() {
        users.put("mike", new UserProfile("mike", "Mike", "Home owner", Set.of("living-room-lights")));
        users.put("iris_admin", new UserProfile("iris_admin", "Aisha", "Admin", Set.of("living-room-lights")));
        users.put("jake", new UserProfile("jake", "Jake", "Guest", Set.of()));
    }

    public Optional<UserProfile> findByFaceId(String faceId) {
        if (faceId == null || faceId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(users.get(faceId.trim().toLowerCase()));
    }

    public Collection<UserProfile> findAll() {
        return users.values();
    }
}
