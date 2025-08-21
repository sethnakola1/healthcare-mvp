package com.healthcare.mvp.billing.service;

import com.healthcare.mvp.billing.dto.BillingDto;
import com.healthcare.mvp.billing.dto.CreateBillRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BillingService {
    
    public BillingDto createBill(CreateBillRequest request) {
        // Calculate totals
        BigDecimal subtotal = request.getConsultationFee()
            .add(request.getTestCharges())
            .add(request.getProcedureCharges())
            .add(request.getMedicationCharges())
            .add(request.getOtherCharges());
        
        BigDecimal taxAmount = subtotal.multiply(request.getTaxPercentage().divide(new BigDecimal("100")));
        BigDecimal discountAmount = subtotal.multiply(request.getDiscountPercentage().divide(new BigDecimal("100")));
        BigDecimal totalAmount = subtotal.add(taxAmount).subtract(discountAmount);
        
        BillingDto dto = new BillingDto();
        dto.setBillingId(UUID.randomUUID());
        dto.setBillNumber("BILL" + String.format("%05d", System.currentTimeMillis() % 100000));
        dto.setPatientId(request.getPatientId());
        dto.setPatientName("Patient Name"); // This should come from patient service
        dto.setHospitalId(request.getHospitalId());
        dto.setAppointmentId(request.getAppointmentId());
        dto.setBillDate(LocalDate.now());
        
        // Set charges
        dto.setConsultationFee(request.getConsultationFee());
        dto.setTestCharges(request.getTestCharges());
        dto.setProcedureCharges(request.getProcedureCharges());
        dto.setMedicationCharges(request.getMedicationCharges());
        dto.setOtherCharges(request.getOtherCharges());
        
        // Set totals
        dto.setSubtotal(subtotal);
        dto.setTaxPercentage(request.getTaxPercentage());
        dto.setTaxAmount(taxAmount);
        dto.setDiscountPercentage(request.getDiscountPercentage());
        dto.setDiscountAmount(discountAmount);
        dto.setTotalAmount(totalAmount);
        
        // Set payment info
        dto.setPaymentStatus("PENDING");
        dto.setPaymentMethod(request.getPaymentMethod());
        dto.setInsuranceProvider(request.getInsuranceProvider());
        dto.setNotes(request.getNotes());
        
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        
        return dto;
    }
    
    public List<BillingDto> getHospitalBills(UUID hospitalId) {
        // In real implementation, fetch from database
        List<BillingDto> bills = new ArrayList<>();
        
        // Sample data for testing
        BillingDto sampleBill = new BillingDto();
        sampleBill.setBillingId(UUID.randomUUID());
        sampleBill.setBillNumber("BILL00001");
        sampleBill.setHospitalId(hospitalId);
        sampleBill.setPatientName("Sample Patient");
        sampleBill.setBillDate(LocalDate.now());
        sampleBill.setTotalAmount(new BigDecimal("500.00"));
        sampleBill.setPaymentStatus("PENDING");
        sampleBill.setCreatedAt(LocalDateTime.now());
        
        bills.add(sampleBill);
        return bills;
    }
    
    public List<BillingDto> getPatientBills(UUID patientId) {
        // In real implementation, fetch from database
        List<BillingDto> bills = new ArrayList<>();
        
        // Sample data for testing
        BillingDto sampleBill = new BillingDto();
        sampleBill.setBillingId(UUID.randomUUID());
        sampleBill.setBillNumber("BILL00001");
        sampleBill.setPatientId(patientId);
        sampleBill.setPatientName("Sample Patient");
        sampleBill.setBillDate(LocalDate.now());
        sampleBill.setTotalAmount(new BigDecimal("500.00"));
        sampleBill.setPaymentStatus("PENDING");
        sampleBill.setCreatedAt(LocalDateTime.now());
        
        bills.add(sampleBill);
        return bills;
    }
    
    public BillingDto updatePaymentStatus(UUID billId, String paymentStatus) {
        // In real implementation, update in database
        BillingDto dto = new BillingDto();
        dto.setBillingId(billId);
        dto.setPaymentStatus(paymentStatus);
        dto.setUpdatedAt(LocalDateTime.now());
        return dto;
    }
    
    public BillingDto getBillById(UUID billId) {
        // In real implementation, fetch from database
        BillingDto dto = new BillingDto();
        dto.setBillingId(billId);
        dto.setBillNumber("BILL00001");
        dto.setTotalAmount(new BigDecimal("500.00"));
        dto.setPaymentStatus("PENDING");
        return dto;
    }
}