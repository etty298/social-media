package ru.home.user.controller.relationship;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.home.user.dto.relationship.status.RelationshipStatusDto;
import ru.home.user.service.relationship.RelationshipService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class RelationshipController {

    private final RelationshipService relationshipService;

    @GetMapping("/{userId}/relationship/{targetUserId}")
    public ResponseEntity<RelationshipStatusDto> relationship(
            @PathVariable(name = "userId") UUID userId,
            @PathVariable(name = "targetUserId") UUID targetUserId) {

        return relationshipService.relationship(userId, targetUserId);
    }
}
