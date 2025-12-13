package org.eldir.server.repository;

import org.eldir.server.entity.User;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@Profile("server")
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByLogin(String login);
}