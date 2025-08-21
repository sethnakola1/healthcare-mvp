package com.healthcare.mvp.billing.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;

import java.math.BigDecimal;
import java.util.UUID;

public class CreateBillRequest {
    
    @NotNull(message = "Patient ID is required")
    private UUID patientId;
    
    @NotNull(message = "Hospital ID is required")
    private UUID hospitalId;
    
    private UUID appointmentId;
    
    // Service charges
    @DecimalMin(value = "0.0", message = "Consultation fee must be positive")
    private BigDecimal consultationFee = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0", message = "Test charges must be positive")
    private BigDecimal testCharges = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0", message = "Procedure charges must be positive")
    private BigDecimal procedureCharges = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0", message = "Medication charges must be positive")
    private BigDecimal medicationCharges = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0", message = "Other charges must be positive")
    private BigDecimal otherCharges = BigDecimal.ZERO;
    
    // Tax and discount
    @DecimalMin(value = "0.0", message = "Tax percentage must be positive")
    @DecimalMax(value = "100.0", message = "Tax percentage cannot exceed 100%")
    private BigDecimal taxPercentage = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0", message = "Discount percentage must be positive")
    @DecimalMax(value = "100.0", message = "Discount percentage cannot exceed 100%")
    private BigDecimal discountPercentage = BigDecimal.ZERO;
    
    // Payment details
    private String paymentMethod; // CASH, CARD, UPI, NET_BANKING, INSURANCE
    private String insuranceProvider;
    private String insuranceClaimNumber;
    private BigDecimal insuranceApprovedAmount;
    
    private String notes;

    // Constructors
    public CreateBillRequest() {}

    // Getters and Setters
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    
    public UUID getHospitalId() { return hospitalId; }
    public void setHospitalId(UUID hospitalId) { this.hospitalId = hospitalId; }
    
    public UUID getAppointmentId() { return appointmentId; }
    public void setAppointmentId(UUID appointmentId) { this.appointmentId = appointmentId; }
    
    public BigDecimal getConsultationFee() { return consultationFee; }
    public void setConsultationFee(BigDecimal consultationFee) { this.consultationFee = consultationFee; }
    
    public BigDecimal getTestCharges() { return testCharges; }
    public void setTestCharges(BigDecimal testCharges) { this.testCharges = testCharges; }
    
    public BigDecimal getProcedureCharges() { return procedureCharges; }
    public void setProcedureCharges(BigDecimal procedureCharges) { this.procedureCharges = procedureCharges; }
    
    public BigDecimal getMedicationCharges() { return medicationCharges; }
    public void setMedicationCharges(BigDecimal medicationCharges) { this.medicationCharges = medicationCharges; }
    
    public BigDecimal getOtherCharges() { return otherCharges; }
    public void setOtherCharges(BigDecimal otherCharges) { this.otherCharges = otherCharges; }
    
    public BigDecimal getTaxPercentage() { return taxPercentage; }
    public void setTaxPercentage(BigDecimal taxPercentage) { this.taxPercentage = taxPercentage; }
    
    public BigDecimal getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(BigDecimal discountPercentage) { this.discountPercentage = discountPercentage; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getInsuranceProvider() { return insuranceProvider; }
    public void setInsuranceProvider(String insuranceProvider) { this.insuranceProvider = insuranceProvider; }
    
    public String getInsuranceClaimNumber() { return insuranceClaimNumber; }
    public void setInsuranceClaimNumber(String insuranceClaimNumber) { this.insuranceClaimNumber = insuranceClaimNumber; }
    
    public BigDecimal getInsuranceApprovedAmount() { return insuranceApprovedAmount; }
    public void setInsuranceApprovedAmount(BigDecimal insuranceApprovedAmount) { this.insuranceApprovedAmount = insuranceApprovedAmount; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}