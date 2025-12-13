package org.eldir.server.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "app_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(unique = true, nullable = false)
    private String login;

    @Column(nullable = false)
    private String password;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "current_token")
    private String currentToken;

    @Column(name = "clearance_level")
    private String clearanceLevel;
    public User() {}

    // Приватный конструктор для Билдера
    private User(Builder builder) {
        this.id = builder.id;
        this.login = builder.login;
        this.password = builder.password;
        this.ipAddress = builder.ipAddress;
        this.currentToken = builder.currentToken;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String login;
        private String password;
        private String ipAddress;
        private String currentToken;
        private String clearanceLevel;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder login(String login) { this.login = login; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }
        public Builder currentToken(String currentToken) { this.currentToken = currentToken; return this; }
        public Builder clearanceLevel(String level) { this.clearanceLevel = level; return this; }

        public User build() {
            return new User(this);
        }
    }

    // Геттеры и Сеттеры
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getCurrentToken() { return currentToken; }
    public void setCurrentToken(String currentToken) { this.currentToken = currentToken; }

    public String getClearanceLevel() { return clearanceLevel; }
    public void setClearanceLevel(String clearanceLevel) { this.clearanceLevel = clearanceLevel; }

}