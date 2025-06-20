package com.example.oyl.domain;

public enum UserRole {
    USER(1, "ROLE_USER"),
    ADMIN(9, "ROLE_ADMIN");

    private final int code;
    private final String roleName;

    UserRole(int code, String roleName) {
        this.code = code;
        this.roleName = roleName;
    }

    public static String fromCode(int code) {
        for (UserRole role : values()) {
            if (role.code == code) return role.roleName;
        }
        throw new IllegalArgumentException("Unknown role code: " + code);
    }
}
