package ru.home.user.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "user",
        indexes = {
                @Index(name = "idx_username", columnList = "username"),
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "name", length = 100)
    @Builder.Default
    private String name = "";

    @Column(name = "bio", length = 100)
    @Builder.Default
    private String bio = "";

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Column(name = "followers_count", nullable = false)
    @Builder.Default
    private Long followersCount = 0L;

    @Column(name = "followings_count", nullable = false)
    @Builder.Default
    private Long followingsCount = 0L;

    @Column(name = "friends_count", nullable = false)
    @Builder.Default
    private Long friendsCount = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}