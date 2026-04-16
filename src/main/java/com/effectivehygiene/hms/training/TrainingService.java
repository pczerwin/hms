package com.effectivehygiene.hms.training;


import com.effectivehygiene.hms.document.DocumentReference;
import com.effectivehygiene.hms.document.DocumentVersion;
import com.effectivehygiene.hms.document.DocumentVersionRepository;
import com.effectivehygiene.hms.domain.exception.EntityNotFoundException;
import com.effectivehygiene.hms.domain.exception.InactiveEntityException;
import com.effectivehygiene.hms.domain.exception.InvalidTrainingException;
import com.effectivehygiene.hms.employee.Employee;
import com.effectivehygiene.hms.employee.EmployeeRepository;
import com.effectivehygiene.hms.training.dto.CreateTrainingInstanceRequest;
import com.effectivehygiene.hms.training.dto.TrainingInstanceMultiResponse;
import com.effectivehygiene.hms.training.dto.TrainingInstanceSingleResponse;
import com.effectivehygiene.hms.training.dto.TrainingMapper;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class TrainingService {

    private final TrainingInstanceRepository trainingInstanceRepository;
    private final TrainingDocumentRepository trainingDocumentRepository;
    private final TrainingTraineeRepository trainingTraineeRepository;
    private final EmployeeRepository employeeRepository;
    private final DocumentVersionRepository documentVersionRepository;

    public TrainingService(
            TrainingInstanceRepository trainingInstanceRepository,
            TrainingDocumentRepository trainingDocumentRepository,
            TrainingTraineeRepository trainingTraineeRepository,
            EmployeeRepository employeeRepository,
            DocumentVersionRepository documentVersionRepository) {
        this.trainingInstanceRepository = trainingInstanceRepository;
        this.trainingDocumentRepository = trainingDocumentRepository;
        this.trainingTraineeRepository = trainingTraineeRepository;
        this.employeeRepository = employeeRepository;
        this.documentVersionRepository = documentVersionRepository;
    }
    // Create instance
    public TrainingInstanceSingleResponse createTrainingInstance(CreateTrainingInstanceRequest request) {
        if (request == null) {
            throw new InvalidTrainingException("Training request is required");
        }

        TrainingInstance newInstance = TrainingMapper.toEntity(request);
        validateRequiredFields(newInstance);
        validateDateSequence(newInstance);

        Set<Long> employeeIds = normalizeAndValidateIds(
                request.getEmployeeIds(),
                "employee IDs are required",
                "At least one employee must attend the training",
                "Employee IDs must not contain null values",
                "Duplicate employee IDs are not allowed in one training instance"
        );

        Set<Long> documentVersionIds = normalizeAndValidateIds(
                request.getDocumentVersionIds(),
                "Document version IDs are required",
                "At least one document version must be covered in the training",
                "Document version IDs must not contain null values",
                "Duplicate document version IDs are not allowed in one training instance"
        );

        List<Employee> employees = loadAndValidateEmployees(employeeIds);
        List<DocumentVersion> documentVersions = loadAndValidateDocumentVersions(documentVersionIds);

        TrainingInstance savedInstance = trainingInstanceRepository.save(newInstance);
        saveTrainingTrainees(savedInstance, employees);
        saveTrainingDocuments(savedInstance, documentVersions);

        List<Long> savedEmployeeIds = employees.stream().map(Employee::getId).toList();
        List<Long> savedDocumentVersionIds = documentVersions.stream().map(DocumentVersion::getId).toList();

        return TrainingMapper.toSingleResponse(savedInstance, savedEmployeeIds, savedDocumentVersionIds);
    }

    private void saveTrainingTrainees(TrainingInstance trainingInstance, List<Employee> employees) {
        List<TrainingTrainee> trainees = new ArrayList<>(employees.size());
        for (Employee employee : employees) {
            TrainingTrainee trainee = new TrainingTrainee();
            trainee.setTrainingInstance(trainingInstance);
            trainee.setEmployee(employee);
            trainees.add(trainee);
        }
        trainingTraineeRepository.saveAll(trainees);
    }

    private void saveTrainingDocuments(TrainingInstance trainingInstance, List<DocumentVersion> documentVersions) {
        List<TrainingDocument> trainingDocuments = new ArrayList<>(documentVersions.size());
        for (DocumentVersion documentVersion : documentVersions) {
            TrainingDocument trainingDocument = new TrainingDocument();
            trainingDocument.setTrainingInstance(trainingInstance);
            trainingDocument.setDocumentVersion(documentVersion);
            trainingDocuments.add(trainingDocument);
        }
        trainingDocumentRepository.saveAll(trainingDocuments);
    }

    private Set<Long> normalizeAndValidateIds(
            List<Long> rawIds,
            String missingListMessage,
            String emptyListMessage,
            String nullElementMessage,
            String duplicateMessage
    ) {
        if (rawIds == null) {
            throw new InvalidTrainingException(missingListMessage);
        }
        if (rawIds.isEmpty()) {
            throw new InvalidTrainingException(emptyListMessage);
        }
        if (rawIds.contains(null)) {
            throw new InvalidTrainingException(nullElementMessage);
        }

        Set<Long> normalizedIds = new LinkedHashSet<>(rawIds);
        if (normalizedIds.size() != rawIds.size()) {
            throw new InvalidTrainingException(duplicateMessage);
        }

        return normalizedIds;
    }

    private List<Employee> loadAndValidateEmployees(Set<Long> employeeIds) {
        List<Employee> employees = employeeRepository.findAllById(employeeIds);
        if (employees.size() != employeeIds.size()) {
            throw new EntityNotFoundException("One or more employees were not found");
        }

        for (Employee employee : employees) {
            if (!employee.isActive()) {
                throw new InactiveEntityException(
                        "Cannot create training instance: employee ID " + employee.getId() + " is inactive"
                );
            }
        }

        return employees;
    }

    private List<DocumentVersion> loadAndValidateDocumentVersions(Set<Long> documentVersionIds) {
        List<DocumentVersion> documentVersions = documentVersionRepository.findAllByIdInWithReference(documentVersionIds);
        if (documentVersions.size() != documentVersionIds.size()) {
            throw new EntityNotFoundException("One or more document versions were not found");
        }

        for (DocumentVersion documentVersion : documentVersions) {
            DocumentReference reference = documentVersion.getDocumentReference();
            if (!reference.isActive()) {
                throw new InactiveEntityException(
                        "Cannot create training instance: reference ID " + reference.getId() + " is inactive"
                );
            }
            if (!documentVersion.isCurrent()) {
                throw new InvalidTrainingException(
                        "Cannot create training instance: documentVersion ID " + documentVersion.getId() + " is not current"
                );
            }
        }

        return documentVersions;
    }

    private void validateRequiredFields(TrainingInstance trainingInstance) {
        if (trainingInstance.getTrainerName() == null
                || trainingInstance.getTrainerType() == null
                || trainingInstance.getTrainingStartDate() == null
                || trainingInstance.getTrainingEndDate() == null
                || trainingInstance.getTrainingDuration() == null
                || trainingInstance.getTrainingExpiryDate() == null
                || trainingInstance.getTrainerSignature() == null) {
            throw new InvalidTrainingException("Training request is missing required fields");
        }
    }

    private void validateDateSequence(TrainingInstance trainingInstance) {
        // Business rule: start <= end <= expiry
        if (trainingInstance.getTrainingEndDate().isBefore(trainingInstance.getTrainingStartDate())) {
            throw new InvalidTrainingException("Training end date must be on or after training start date");
        }
        if (trainingInstance.getTrainingExpiryDate().isBefore(trainingInstance.getTrainingEndDate())) {
            throw new InvalidTrainingException("Training expiry date must be on or after training end date");
        }
    }






    // Find all training instances (no pagination for current MVP stage).
    @Transactional(readOnly = true)
    public List<TrainingInstanceMultiResponse> findAll() {
        List<TrainingInstance> instances = trainingInstanceRepository.findAll();
        if (instances.isEmpty()) {
            return List.of();
        }

        List<Long> instanceIds = instances.stream().map(TrainingInstance::getId).toList();

        Map<Long, List<Long>> employeesByInstance = trainingTraineeRepository
                .findByTrainingInstanceIdIn(instanceIds).stream()
                .collect(Collectors.groupingBy(
                        tt -> tt.getTrainingInstance().getId(),
                        Collectors.mapping(tt -> tt.getEmployee().getId(), Collectors.toList())
                ));

        Map<Long, List<Long>> documentsByInstance = trainingDocumentRepository
                .findByTrainingInstanceIdIn(instanceIds).stream()
                .collect(Collectors.groupingBy(
                        td -> td.getTrainingInstance().getId(),
                        Collectors.mapping(td -> td.getDocumentVersion().getId(), Collectors.toList())
                ));

        return instances.stream()
                .map(instance -> TrainingMapper.toMultiResponse(
                        instance,
                        employeesByInstance.getOrDefault(instance.getId(), List.of()),
                        documentsByInstance.getOrDefault(instance.getId(), List.of())
                ))
                .toList();
    }

    // Find by id
    @Transactional(readOnly = true)
    public TrainingInstanceSingleResponse findByTrainingInstanceId(Long id) {
        TrainingInstance trainingInstance = trainingInstanceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Training instance not found: id=" + id));

        List<Long> employeeIds = trainingTraineeRepository
                .findByTrainingInstanceId(id).stream()
                .map(tt -> tt.getEmployee().getId())
                .toList();

        List<Long> documentVersionIds = trainingDocumentRepository
                .findByTrainingInstanceId(id).stream()
                .map(td -> td.getDocumentVersion().getId())
                .toList();

        return TrainingMapper.toSingleResponse(trainingInstance, employeeIds, documentVersionIds);
    }





}
