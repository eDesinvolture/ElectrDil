package org.eldir.server.config;

import jakarta.annotation.PostConstruct;
import org.eldir.server.entity.EavAttribute;
import org.eldir.server.entity.User;
import org.eldir.server.repository.AttributeRepository;
import org.eldir.server.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Profile("server")
public class DatabaseInitializer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final AttributeRepository attributeRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Конструктор (вместо @RequiredArgsConstructor)
    public DatabaseInitializer(AttributeRepository attributeRepository, UserRepository userRepository) {
        this.attributeRepository = attributeRepository;
        this.userRepository = userRepository;
    }

    @PostConstruct
    @Transactional
    public void init() {
        log.info("Checking database initialization...");

        initAttributes();
        initDefaultAdmin();

        log.info("Database initialization finished.");
    }

    private void initAttributes() {
        List<EavAttribute> attributes = List.of(
                new EavAttribute("title", "Название документа"),
                new EavAttribute("author_name", "Имя автора (текстом)"),
                new EavAttribute("is_hypercube", "Включает режим гиперкуба"),
                new EavAttribute("description", "Описание содержимого"),
                new EavAttribute("file_type_code", "Код типа файла (ENUM)"),
                new EavAttribute("access_level_code", "Код уровня доступа (ENUM)")
        );

        for (EavAttribute attr : attributes) {
            if (!attributeRepository.existsById(attr.getCode())) {
                attributeRepository.save(attr);
                log.info("Created attribute: {}", attr.getCode());
            }
        }
    }

    private void initDefaultAdmin() {
        if (userRepository.findByLogin("admin").isEmpty()) {
            // Используем наш самописный Builder
            User admin = User.builder()
                    .login("admin")
                    .password(passwordEncoder.encode("admin")) // Пароль: admin
                    .ipAddress("127.0.0.1")
                    .build();

            userRepository.save(admin);
            log.info("Created default admin user (login: admin, pass: admin)");
        }
    }
}