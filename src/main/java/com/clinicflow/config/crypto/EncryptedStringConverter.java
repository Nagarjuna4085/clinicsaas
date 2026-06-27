package com.clinicflow.config.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Transparently encrypts/decrypts a String column at rest. Apply with @Convert. */
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return FieldCrypto.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return FieldCrypto.decrypt(dbData);
    }
}
