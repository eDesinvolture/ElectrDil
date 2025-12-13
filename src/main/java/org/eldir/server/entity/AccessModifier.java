package org.eldir.server.entity;

import lombok.Getter;

@Getter
public enum AccessModifier {
    PUBLIC("public"),
    DSP("dsp"), // Для служебного пользования
    SECRET("secret"),
    TOP_SECRET("top_secret"),
    BURN_AFTER_READING("burn_after_reading");

    private final String code;

    AccessModifier(String code) { this.code = code; }
}