package com.poli.polirestaurante.security;

import org.springframework.security.crypto.password.PasswordEncoder;

public final class PasswordEncoding {

    private static final String BCRYPT_PATTERN = "^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$";

    private PasswordEncoding() {
    }

    public static String encodeIfNeeded(String password, PasswordEncoder passwordEncoder) {
        if (password == null) {
            return null;
        }
        boolean hashBcryptValido = password.matches(BCRYPT_PATTERN);
        return !hashBcryptValido || passwordEncoder.upgradeEncoding(password)
                ? passwordEncoder.encode(password)
                : password;
    }
}