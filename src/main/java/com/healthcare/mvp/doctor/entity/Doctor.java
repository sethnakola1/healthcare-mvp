package com.healthcare.mvp.doctor.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {

    @Id
    private UUID doctorId;
    private String firstName;
    private String lastName;
    private String specialization;
    public void setHospitalId(UUID hospitalId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setHospitalId'");
    }
}
