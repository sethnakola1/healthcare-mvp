package com.healthcare.mvp.shared.constants;

public enum BusinessRole {
    SUPER_ADMIN("Super Admin", "Full system access and management"),
    TECH_ADVISOR("Tech Advisor", "Technical advisor for hospital partnerships"),
    HOSPITAL_ADMIN("Hospital Admin", "Hospital administration and management"),
    DOCTOR("Doctor", "Medical practitioner"),
    NURSE("Nurse", "Nursing staff"),
    RECEPTIONIST("Receptionist", "Front desk and appointment management"),
    PATIENT("Patient", "Patient access to medical services");

    private final String displayName;
    private final String description;

    BusinessRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean hasAdminAccess() {
        return this == SUPER_ADMIN || this == HOSPITAL_ADMIN;
    }

    public boolean isMedicalStaff() {
        return this == DOCTOR || this == NURSE;
    }

    public boolean canManageAppointments() {
        return this == HOSPITAL_ADMIN || this == DOCTOR || this == NURSE || this == RECEPTIONIST;
    }
}