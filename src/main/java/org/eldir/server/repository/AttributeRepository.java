package org.eldir.server.repository;

import org.eldir.server.entity.EavAttribute;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile("server")
public interface AttributeRepository extends JpaRepository<EavAttribute, String> {
}