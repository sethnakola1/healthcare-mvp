//package com.healthcare.mvp.ehr.service;
//
//import com.healthcare.mvp.patient.entity.Patient;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class EHRIntegrationService {
//    private final FHIRClient fhirClient;
//
//    public void syncPatient(Patient patient) {
//        IGenericClient client = fhirClient.getClient();
//        Patient fhirPatient = new Patient();
//        fhirPatient.addName().setFamily(patient.getLastName()).addGiven(patient.getFirstName());
//        client.create().resource(fhirPatient).execute();
//    }
//}