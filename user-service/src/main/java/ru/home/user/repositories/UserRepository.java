package ru.home.user.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.home.user.entities.UserEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByUsername(String username);

    Page<UserEntity> findAllByUsernameStartsWithIgnoreCase(String username, Pageable pageable);

    Page<UserEntity> findAllBy(Pageable pageable);

    Page<UserEntity> findAllByNameStartsWithIgnoreCase(String name, Pageable pageable);
}
