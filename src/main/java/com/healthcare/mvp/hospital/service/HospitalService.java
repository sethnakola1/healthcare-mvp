package com.healthcare.mvp.hospital.service;

import com.healthcare.mvp.hospital.dto.CreateHospitalRequest;
import com.healthcare.mvp.hospital.dto.HospitalDto;
import com.healthcare.mvp.hospital.entity.Hospital;
import com.healthcare.mvp.hospital.repository.HospitalRepository;
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

    public HospitalDto createHospital(CreateHospitalRequest request, UUID createdByBusinessUserId) {
        Hospital hospital = new Hospital();
        hospital.setHospitalName(request.getHospitalName());
        hospital.setAddress(request.getAddress());
        hospital.setPartnerCodeUsed(request.getPartnerCodeUsed()); // Assume this field exists
        return convertToDto(hospitalRepository.save(hospital));
    }

    public List<HospitalDto> getAllHospitals() {
        return hospitalRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<HospitalDto> getHospitalsByBusinessUser(String partnerCodeUsed) {
        return hospitalRepository.findByPartnerCodeUsed(partnerCodeUsed) ;
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
        hospital.setIsActive(false); // Assume an 'active' field exists
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

}