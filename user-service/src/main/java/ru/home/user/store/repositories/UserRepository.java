package ru.home.user.store.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.home.user.store.entities.UserEntity;

import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByUsernameEquals(String username);

    Stream<UserEntity> streamAllByNameStartsWithIgnoreCase(String name);

    Stream<UserEntity> streamAllByUsernameStartsWithIgnoreCase(String username);

    Stream<UserEntity> streamAllBy();

}
