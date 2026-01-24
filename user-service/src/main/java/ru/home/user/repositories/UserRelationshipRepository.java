package ru.home.user.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.home.user.dto.relationship.lists.FollowProjection;
import ru.home.user.dto.relationship.lists.FriendProjection;
import ru.home.user.entities.UserRelationshipEntity;

import java.util.Optional;
import java.util.UUID;

public interface UserRelationshipRepository extends JpaRepository<UserRelationshipEntity, UUID> {

    boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    Optional<UserRelationshipEntity> findByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    @Modifying
    void deleteByFollowerIdAndFollowingId(UUID followerId, UUID followingId);



    @Query("""
                SELECT ur1.followingId 
                FROM UserRelationshipEntity ur1
                WHERE ur1.followerId = :userId
                AND EXISTS (
                    SELECT 1 FROM UserRelationshipEntity ur2
                    WHERE ur2.followerId = ur1.followingId
                    AND ur2.followingId = :userId
                )
            """)
    Page<UUID> findMutualFollowingIds(@Param("userId") UUID userId, Pageable pageable);

    // Единичная проверка (быстрая)
    @Query("""
            SELECT CASE WHEN COUNT(ur) = 2 THEN true ELSE false END
            FROM UserRelationshipEntity ur
            WHERE (ur.followerId = :userId1 AND ur.followingId = :userId2)
            OR (ur.followerId = :userId2 AND ur.followingId = :userId1)
            """)
    boolean areFriends(@Param("userId1") UUID userId1, @Param("userId2") UUID userId2);


    @Query("""
            SELECT NEW ru.home.user.dto.relationship.lists.FollowProjection(u.id, u.username, u.name, u.bio, ur.updatedAt)
            FROM UserRelationshipEntity ur
            JOIN UserEntity u
                ON ur.followerId = u.id
            WHERE ur.followingId = :userId
            """)
    Page<FollowProjection> findFollowers(@Param("userId") UUID userId, Pageable pageable);

    @Query("""
            SELECT NEW ru.home.user.dto.relationship.lists.FollowProjection(u.id, u.username, u.name, u.bio, ur.updatedAt)
            FROM UserRelationshipEntity ur
            JOIN UserEntity u
                ON ur.followingId = u.id
            WHERE ur.followerId = :userId
            """)
    Page<FollowProjection> findFollowings(@Param("userId") UUID userId, Pageable pageable);

    // Список всех друзей (эффективно через JOIN)
    @Query("""
            SELECT  NEW ru.home.user.dto.relationship.lists.FriendProjection(u.id, u.username, u.name, u.bio, ur1.updatedAt)
            FROM UserRelationshipEntity ur1
            JOIN UserRelationshipEntity ur2
                ON ur1.followerId = ur2.followingId
                AND ur1.followingId = ur2.followerId
            JOIN UserEntity u
                ON u.id = ur1.followingId
            WHERE ur1.followerId = :userId
            """)
    Page<FriendProjection> findFriends(@Param("userId") UUID userId, Pageable pageable);
}
