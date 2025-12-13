package org.eldir.server.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "eav_attributes")
public class EavAttribute {

    @Id
    @Column(name = "attr_code")
    private String code;

    private String description;

    public EavAttribute() {
    }

    public EavAttribute(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}