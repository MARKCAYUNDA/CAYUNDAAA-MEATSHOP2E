package com.scc.model;

public record User(int user_id, String full_name, String username, String password, String role, String status) {
    @Override
    public String toString() {
        return String.format("[%d] %s (%s) - Role: %s | Status: %s", user_id, full_name, username, role, status);
    }
}