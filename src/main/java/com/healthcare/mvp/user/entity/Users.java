package com.healthcare.mvp.user.entity;

import com.healthcare.mvp.business.entity.BusinessUser;
import com.healthcare.mvp.hospital.entity.Hospital;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
public class Users {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "hospital_id", nullable = false)
    private UUID hospitalId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "encrypted_firstname", nullable = false)
    private byte[] encryptedFirstname;

    @Column(name = "encrypted_lastname", nullable = false)
    private byte[] encryptedLastname;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "encrypted_password", nullable = false)
    private byte[] encryptedPassword;

    @Column(name = "role", nullable = false)
    private String role; // e.g., DOCTOR, HOSPITAL_ADMIN, STAFF

    @Column(name = "is_email_verified", nullable = false)
    private boolean isEmailVerified = false;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "is_encrypted", nullable = false)
    private boolean isEncrypted = true;

    @Column(name = "encryption_key_id")
    private UUID encryptionKeyId;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false)
    private OffsetDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date", nullable = false)
    private OffsetDateTime updatedDate;

    @Version
    private Long version;

    @ManyToOne
    @JoinColumn(name = "hospital_id", insertable = false, updatable = false)
    private Hospital hospital;

    @ManyToOne
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private BusinessUser createdByAdmin;

    @ManyToOne
    @JoinColumn(name = "updated_by", insertable = false, updatable = false)
    private BusinessUser updatedByAdmin;
}