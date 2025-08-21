//package com.healthcare.mvp.patient;
//
//import com.healthcare.mvp.patient.dto.CreatePatientRequest;
//import com.healthcare.mvp.shared.dto.BaseResponse;
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.ResponseEntity;
//import org.springframework.test.context.TestPropertySource;
//
//import java.net.http.HttpHeaders;
//import java.time.LocalDate;
//import java.util.UUID;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@TestPropertySource(properties = {
//    "spring.datasource.url=jdbc:h2:mem:testdb",
//    "spring.jpa.hibernate.ddl-auto=create-drop",
//    "logging.level.org.springframework.web=DEBUG"
//})
//class PatientControllerIntegrationTest {
//
//    @Autowired
//    private TestRestTemplate restTemplate;
//
//    @Autowired
//    private TestEntityManager entityManager;
//
//    private static UUID testHospitalId;
//    private static String jwtToken;
//
//    @BeforeAll
//    static void setup(@Autowired TestRestTemplate restTemplate) {
//        // Setup test data and authentication
//        // Create test hospital, users, etc.
//    }
//
//    @Test
//    @Order(1)
//    void testCreatePatient_Success() {
//        CreatePatientRequest request = CreatePatientRequest.builder()
//            .hospitalId(testHospitalId)
//            .firstName("John")
//            .lastName("Doe")
//            .dateOfBirth(LocalDate.of(1990, 5, 15))
//            .email("john.doe@email.com")
//            .phoneNumber("+1-555-123-4567")
//            .address("123 Main St, Anytown, ST 12345")
//            .mrn("MRN123456")
//            .gender("MALE")
//            .bloodGroup("O+")
//            .build();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(jwtToken);
//        HttpEntity<CreatePatientRequest> entity = new HttpEntity<>(request, headers);
//
//        ResponseEntity<BaseResponse> response = restTemplate.postForEntity(
//            "/api/patients", entity, BaseResponse.class);
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(response.getBody().isSuccess()).isTrue();
//        assertThat(response.getBody().getMessage()).contains("Patient registered successfully");
//    }
//
//    @Test
//    @Order(2)
//    void testCreatePatient_ValidationError() {
//        CreatePatientRequest request = CreatePatientRequest.builder()
//            .hospitalId(testHospitalId)
//            .firstName("") // Invalid - empty first name
//            .lastName("Doe")
//            .email("invalid-email") // Invalid email format
//            .build();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(jwtToken);
//        HttpEntity<CreatePatientRequest> entity = new HttpEntity<>(request, headers);
//
//        ResponseEntity<BaseResponse> response = restTemplate.postForEntity(
//            "/api/patients", entity, BaseResponse.class);
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
//        assertThat(response.getBody().isSuccess()).isFalse();
//        assertThat(response.getBody().getErrorCode()).isEqualTo("VALIDATION_ERRORS");
//    }
//
//    @Test
//    @Order(3)
//    void testGetPatientById_Success() {
//        // Test fetching patient by ID
//        UUID patientId = createTestPatient();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(jwtToken);
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//
//        ResponseEntity<BaseResponse> response = restTemplate.exchange(
//            "/api/patients/" + patientId, HttpMethod.GET, entity, BaseResponse.class);
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(response.getBody().isSuccess()).isTrue();
//    }
//
//    @Test
//    @Order(4)
//    void testGetPatientById_NotFound() {
//        UUID nonExistentId = UUID.randomUUID();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(jwtToken);
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//
//        ResponseEntity<BaseResponse> response = restTemplate.exchange(
//            "/api/patients/" + nonExistentId, HttpMethod.GET, entity, BaseResponse.class);
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
//    }
//
//    @Test
//    @Order(5)
//    void testGetPatientById_Unauthorized() {
//        UUID patientId = createTestPatient();
//
//        // No JWT token in headers
//        HttpEntity<String> entity = new HttpEntity<>(new HttpHeaders());
//
//        ResponseEntity<BaseResponse> response = restTemplate.exchange(
//            "/api/patients/" + patientId, HttpMethod.GET, entity, BaseResponse.class);
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
//    }
//
//    private UUID createTestPatient() {
//        // Helper method to create test patient
//        return UUID.randomUUID();
//    }
//}