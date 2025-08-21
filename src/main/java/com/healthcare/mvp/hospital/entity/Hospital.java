package com.healthcare.mvp.hospital.entity;

import com.healthcare.mvp.shared.entity.LegacyBaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "hospital")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Hospital extends LegacyBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "hospital_id")
    private UUID hospitalId;

    @Column(name = "hospital_name", nullable = false)
    private String hospitalName;

    @Column(name = "hospital_code", nullable = false, unique = true)
    private String hospitalCode;

    @Column(name = "license_number")
    private String licenseNumber;

    @Column(name = "tax_id")
    private String taxId;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "website")
    private String website;

    @Column(name = "brought_by_business_user")
    private UUID broughtByBusinessUser;

    @Column(name = "partner_code_used")
    private String partnerCodeUsed;

    @Column(name = "tech_support_1")
    private UUID techSupport1;

    @Column(name = "tech_support_2")
    private UUID techSupport2;

    @Column(name = "subscription_plan", nullable = false)
    private String subscriptionPlan;

    @Column(name = "monthly_revenue"
//            , precision = 12, scale = 2
    )
    private Double monthlyRevenue;

    @Column(name = "commission_rate"
//            , precision = 5, scale = 2
    )
    private Double commissionRate;

    @Column(name = "contract_start_date")
    private LocalDate contractStartDate;

    @Column(name = "contract_end_date")
    private LocalDate contractEndDate;
}