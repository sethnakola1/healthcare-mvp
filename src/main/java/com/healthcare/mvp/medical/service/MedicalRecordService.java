package com.healthcare.mvp.medical.service;

import com.healthcare.mvp.medical.dto.CreateMedicalRecordRequest;
import com.healthcare.mvp.medical.dto.MedicalRecordDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MedicalRecordService {
    
    public MedicalRecordDto createMedicalRecord(CreateMedicalRecordRequest request) {
        MedicalRecordDto dto = new MedicalRecordDto();
        dto.setMedicalRecordId(UUID.randomUUID());
        dto.setPatientId(request.getPatientId());
        dto.setPatientName("Patient Name"); // This should come from patient service
        dto.setDoctorId(UUID.randomUUID()); // This should come from current user context
        dto.setDoctorName("Dr. Sample"); // This should come from user service
        dto.setHospitalId(request.getHospitalId());
        dto.setAppointmentId(request.getAppointmentId());
        dto.setRecordDate(LocalDateTime.now());
        
        // Set medical details
        dto.setChiefComplaint(request.getChiefComplaint());
        dto.setPresentIllness(request.getPresentIllness());
        dto.setPhysicalExamination(request.getPhysicalExamination());
        dto.setDiagnosis(request.getDiagnosis());
        dto.setDifferentialDiagnosis(request.getDifferentialDiagnosis());
        dto.setTreatmentPlan(request.getTreatmentPlan());
        dto.setFollowUpInstructions(request.getFollowUpInstructions());
        dto.setVitalSigns(request.getVitalSigns());
        dto.setDoctorNotes(request.getDoctorNotes());
        dto.setRecommendations(request.getRecommendations());
        
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        
        return dto;
    }
    
    public List<MedicalRecordDto> getPatientMedicalHistory(UUID patientId) {
        // In real implementation, fetch from database
        List<MedicalRecordDto> records = new ArrayList<>();
        
        // Sample data for testing
        MedicalRecordDto sampleRecord = new MedicalRecordDto();
        sampleRecord.setMedicalRecordId(UUID.randomUUID());
        sampleRecord.setPatientId(patientId);
        sampleRecord.setPatientName("Sample Patient");
        sampleRecord.setDoctorName("Dr. Sample");
        sampleRecord.setRecordDate(LocalDateTime.now());
        sampleRecord.setChiefComplaint("Fever and headache");
        sampleRecord.setDiagnosis("Viral fever");
        sampleRecord.setTreatmentPlan("Rest and medication");
        sampleRecord.setCreatedAt(LocalDateTime.now());
        
        records.add(sampleRecord);
        return records;
    }
    
    public Optional<MedicalRecordDto> getMedicalRecordById(UUID recordId) {
        // In real implementation, fetch from database
        MedicalRecordDto dto = new MedicalRecordDto();
        dto.setMedicalRecordId(recordId);
        dto.setPatientName("Sample Patient");
        dto.setDoctorName("Dr. Sample");
        dto.setChiefComplaint("Sample complaint");
        dto.setDiagnosis("Sample diagnosis");
        dto.setCreatedAt(LocalDateTime.now());
        
        return Optional.of(dto);
    }
    
    public MedicalRecordDto updateMedicalRecord(UUID recordId, CreateMedicalRecordRequest request) {
        // In real implementation, update in database
        MedicalRecordDto dto = new MedicalRecordDto();
        dto.setMedicalRecordId(recordId);
        dto.setPatientId(request.getPatientId());
        dto.setChiefComplaint(request.getChiefComplaint());
        dto.setDiagnosis(request.getDiagnosis());
        dto.setTreatmentPlan(request.getTreatmentPlan());
        dto.setUpdatedAt(LocalDateTime.now());
        
        return dto;
    }
    
    public List<MedicalRecordDto> getMedicalRecordsByDoctor(UUID doctorId) {
        // In real implementation, fetch from database
        List<MedicalRecordDto> records = new ArrayList<>();
        
        MedicalRecordDto sampleRecord = new MedicalRecordDto();
        sampleRecord.setMedicalRecordId(UUID.randomUUID());
        sampleRecord.setDoctorId(doctorId);
        sampleRecord.setDoctorName("Dr. Sample");
        sampleRecord.setPatientName("Sample Patient");
        sampleRecord.setRecordDate(LocalDateTime.now());
        sampleRecord.setChiefComplaint("Sample complaint");
        sampleRecord.setDiagnosis("Sample diagnosis");
        
        records.add(sampleRecord);
        return records;
    }
}
