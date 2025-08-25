package com.healthcare.mvp.hospital.service;

import com.healthcare.mvp.business.dto.BusinessUserDto;
import com.healthcare.mvp.business.entity.BusinessUser;
import com.healthcare.mvp.business.repository.BusinessUserRepository;
import com.healthcare.mvp.hospital.dto.CreateHospitalRequest;
import com.healthcare.mvp.hospital.dto.HospitalDto;
import com.healthcare.mvp.hospital.entity.Hospital;
import com.healthcare.mvp.hospital.repository.HospitalRepository;
import com.healthcare.mvp.shared.constants.BusinessRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class HospitalService {

    private final HospitalRepository hospitalRepository;
    private final BusinessUserRepository businessUserRepository;

    public HospitalDto createHospital(CreateHospitalRequest request, UUID createdByBusinessUserId) {
        Hospital hospital = new Hospital();
        hospital.setHospitalName(request.getHospitalName());
        hospital.setAddress(request.getAddress());
        hospital.setPartnerCodeUsed(request.getPartnerCodeUsed());
        return convertToDto(hospitalRepository.save(hospital));
    }

    public List<HospitalDto> getAllHospitals() {
        return hospitalRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * ADDED: Get all hospitals as business user DTOs for SuperAdminController - FIXED
     * This method returns BusinessUserDto instead of HospitalDto to satisfy the controller requirement
     */
    public List<BusinessUserDto> getAllHospitalsAsBusinessUsers() {
        // Since hospitals are not business users, we need to return the business users
        // who are hospital admins or related to hospitals
        return businessUserRepository.findByBusinessRole(BusinessRole.HOSPITAL_ADMIN)
                .stream()
                .map(this::convertBusinessUserToDto)
                .collect(Collectors.toList());
    }

    public List<HospitalDto> getHospitalsByBusinessUser(String partnerCodeUsed) {
        return hospitalRepository.findByPartnerCodeUsed(partnerCodeUsed);
    }

    public Optional<HospitalDto> getHospitalById(UUID hospitalId) {
        return hospitalRepository.findById(hospitalId)
                .map(this::convertToDto);
    }

    public Optional<HospitalDto> getHospitalByCode(String hospitalCode) {
        return hospitalRepository.findByHospitalCode(hospitalCode)
                .map(this::convertToDto);
    }

    public HospitalDto updateHospital(UUID hospitalId, CreateHospitalRequest request) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new RuntimeException("Hospital not found"));
        hospital.setHospitalName(request.getHospitalName());
        hospital.setAddress(request.getAddress());
        hospital.setPartnerCodeUsed(request.getPartnerCodeUsed());
        return convertToDto(hospitalRepository.save(hospital));
    }

    public void deactivateHospital(UUID hospitalId) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new RuntimeException("Hospital not found"));
        hospital.setIsActive(false);
        hospitalRepository.save(hospital);
    }

    private HospitalDto convertToDto(Hospital hospital) {
        HospitalDto dto = new HospitalDto();
        dto.setHospitalId(hospital.getHospitalId());
        dto.setHospitalName(hospital.getHospitalName());
        dto.setAddress(hospital.getAddress());
        dto.setPartnerCodeUsed(hospital.getPartnerCodeUsed());
        return dto;
    }

    private Hospital convertToEntity(HospitalDto hospitalDto) {
        Hospital entity = new Hospital();
        entity.setHospitalId(hospitalDto.getHospitalId());
        entity.setHospitalName(hospitalDto.getHospitalName());
        entity.setAddress(hospitalDto.getAddress());
        entity.setPartnerCodeUsed(hospitalDto.getPartnerCodeUsed());
        return entity;
    }

    /**
     * ADDED: Convert BusinessUser to BusinessUserDto for getAllHospitalsAsBusinessUsers method
     */
    private BusinessUserDto convertBusinessUserToDto(BusinessUser businessUser) {
        BusinessUserDto dto = new BusinessUserDto();
        dto.setBusinessUserId(businessUser.getBusinessUserId());
        dto.setUsername(businessUser.getUsername());
        dto.setEmail(businessUser.getEmail());
        dto.setFirstName(businessUser.getFirstName());
        dto.setLastName(businessUser.getLastName());
        dto.setPhoneNumber(businessUser.getPhoneNumber());
        dto.setBusinessRole(businessUser.getBusinessRole());
        dto.setPartnerCode(businessUser.getPartnerCode());
        dto.setCommissionPercentage(businessUser.getCommissionPercentage());
        dto.setTerritory(businessUser.getTerritory());
        dto.setTargetHospitalsMonthly(businessUser.getTargetHospitalsMonthly());
        dto.setTotalHospitalsBrought(businessUser.getTotalHospitalsBrought());
        dto.setTotalCommissionEarned(businessUser.getTotalCommissionEarned());
        dto.setIsActive(businessUser.getIsActive());
        dto.setEmailVerified(businessUser.getEmailVerified());
        dto.setLastLogin(businessUser.getLastLogin());
        dto.setCreatedAt(businessUser.getCreatedAt());
        return dto;
    }
}