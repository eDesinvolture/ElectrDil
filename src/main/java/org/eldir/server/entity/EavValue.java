package org.eldir.server.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "eav_values")
public class EavValue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private EavDocument document;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "attribute_code", nullable = false)
    private EavAttribute attribute;

    @Column(name = "value_string", length = 4096)
    private String value;

    public EavValue() {}

    // Геттеры и Сеттеры
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public EavDocument getDocument() { return document; }
    public void setDocument(EavDocument document) { this.document = document; }

    public EavAttribute getAttribute() { return attribute; }
    public void setAttribute(EavAttribute attribute) { this.attribute = attribute; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}