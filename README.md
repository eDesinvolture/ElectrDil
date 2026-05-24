Учебный проект клиент-серверной системы

Стек
Java 25, Spring Boot 3.5.7, Spring Security, Spring Data JPA, Spring AOP, Actuator
gRPC 1.71.0, Protobuf 3.25.5
JavaFX 25.0.1
JUnit 5, Mockito

Для запуска проекта понадобятся установленные JDK 25, Maven и запущенная СУБД PostgreSQL.

Запуск
Создать базу в PostgreSQL:
CREATE DATABASE eldir_db;

Настройки подключения в src/main/resources/application.properties

Чтобы запустить юнит-тесты из мавена нужно выполнить в консоли(или в IDE нажать)
mvn (clean) test

чтобы запустить сервер нужно запустить класс EldirServerApplication
чтобы запустить клиента нужно запустить класс ClientLauncher
