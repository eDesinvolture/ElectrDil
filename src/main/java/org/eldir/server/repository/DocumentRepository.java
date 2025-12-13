package org.eldir.server.repository;

import org.eldir.server.entity.EavDocument;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@Profile("server")
public interface DocumentRepository extends JpaRepository<EavDocument, UUID> {
    // Можно набубенить фич но стандартных вроде хватит хз
}