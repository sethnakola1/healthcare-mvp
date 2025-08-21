package com.healthcare.mvp.hospital.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class HospitalDto {
    private UUID hospitalId;
    private String hospitalName;
    private String hospitalCode;
    private String licenseNumber;
    private String taxId;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String phoneNumber;
    private String email;
    private String website;
    private String broughtByBusinessUserName;
    private String broughtByPartnerCode;
    private String partnerCodeUsed;
    private String techSupport1Name;
    private String techSupport2Name;
    private String subscriptionPlan;
    private BigDecimal monthlyRevenue;
    private BigDecimal commissionRate;
    private Boolean isActive;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private LocalDateTime createdAt;

    // Constructors
    public HospitalDto() {}

    // Getters and setters
    public UUID getHospitalId() { return hospitalId; }
    public void setHospitalId(UUID hospitalId) { this.hospitalId = hospitalId; }
    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
    public String getHospitalCode() { return hospitalCode; }
    public void setHospitalCode(String hospitalCode) { this.hospitalCode = hospitalCode; }
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getBroughtByBusinessUserName() { return broughtByBusinessUserName; }
    public void setBroughtByBusinessUserName(String broughtByBusinessUserName) { this.broughtByBusinessUserName = broughtByBusinessUserName; }
    public String getBroughtByPartnerCode() { return broughtByPartnerCode; }
    public void setBroughtByPartnerCode(String broughtByPartnerCode) { this.broughtByPartnerCode = broughtByPartnerCode; }
    public String getPartnerCodeUsed() { return partnerCodeUsed; }
    public void setPartnerCodeUsed(String partnerCodeUsed) { this.partnerCodeUsed = partnerCodeUsed; }
    public String getTechSupport1Name() { return techSupport1Name; }
    public void setTechSupport1Name(String techSupport1Name) { this.techSupport1Name = techSupport1Name; }
    public String getTechSupport2Name() { return techSupport2Name; }
    public void setTechSupport2Name(String techSupport2Name) { this.techSupport2Name = techSupport2Name; }
    public String getSubscriptionPlan() { return subscriptionPlan; }
    public void setSubscriptionPlan(String subscriptionPlan) { this.subscriptionPlan = subscriptionPlan; }
    public BigDecimal getMonthlyRevenue() { return monthlyRevenue; }
    public void setMonthlyRevenue(BigDecimal monthlyRevenue) { this.monthlyRevenue = monthlyRevenue; }
    public BigDecimal getCommissionRate() { return commissionRate; }
    public void setCommissionRate(BigDecimal commissionRate) { this.commissionRate = commissionRate; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public LocalDate getContractStartDate() { return contractStartDate; }
    public void setContractStartDate(LocalDate contractStartDate) { this.contractStartDate = contractStartDate; }
    public LocalDate getContractEndDate() { return contractEndDate; }
    public void setContractEndDate(LocalDate contractEndDate) { this.contractEndDate = contractEndDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}