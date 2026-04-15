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
import com.effectivehygiene.hms.training.dto.TrainingMapper;
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
        public TrainingInstance createTrainingInstance(
                CreateTrainingInstanceRequest request
        ) {
        TrainingInstance newInstance = TrainingMapper.toEntity(request);

            // Map non-text fields handled outside mapper.
            newInstance.setTrainerType(request.getTrainerType());
            newInstance.setTrainingStartDate(request.getTrainingStartDate());
            newInstance.setTrainingEndDate(request.getTrainingEndDate());
            newInstance.setTrainingExpiryDate(request.getTrainingExpiryDate());

            Set<Long> employeeIds = new LinkedHashSet<>(request.getEmployeeIds());
            if (employeeIds.size() != request.getEmployeeIds().size()) {
                throw new InvalidTrainingException("Duplicate employee IDs are not allowed in one training instance");
            }

            Set<Long> documentVersionIds = new LinkedHashSet<>(request.getDocumentVersionIds());
            if (documentVersionIds.size() != request.getDocumentVersionIds().size()) {
                throw new InvalidTrainingException("Duplicate document version IDs are not allowed in one training instance");
            }

            // All employees must be active at the time of training
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

            // All document references must be active
            List<DocumentVersion> documentVersions = documentVersionRepository.findAllById(documentVersionIds);
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
            }

            // All document versions must be current
            for (DocumentVersion documentVersion : documentVersions) {
                if (!documentVersion.isCurrent()) {
                    throw new InvalidTrainingException(
                            "Cannot create training instance: documentVersion ID " + documentVersion.getId() + " is not current"
                    );
                }
            }
            // Training start date < training end date
            if (request.getTrainingEndDate().isBefore(request.getTrainingStartDate())) {
                throw new InvalidTrainingException(
                        "Training end date must be after training start date"
                );
            }
            // Training end date < training expiry date
            if (request.getTrainingExpiryDate().isBefore(request.getTrainingEndDate())) {
                throw new InvalidTrainingException(
                        "Training expiry date must be on or after training end date"
                );
            }

            TrainingInstance savedInstance = trainingInstanceRepository.save(newInstance);
            saveTrainingTrainees(savedInstance, employees);
            saveTrainingDocuments(savedInstance, documentVersions);

        return savedInstance;
        }

    private void saveTrainingTrainees(TrainingInstance trainingInstance, List<Employee> employees) {
        List<TrainingTrainee> trainees = new ArrayList<>();
        for (Employee employee : employees) {
            TrainingTrainee trainee = new TrainingTrainee();
            trainee.setTrainingInstance(trainingInstance);
            trainee.setEmployee(employee);
            trainees.add(trainee);
        }
        trainingTraineeRepository.saveAll(trainees);
    }

    private void saveTrainingDocuments(TrainingInstance trainingInstance, List<DocumentVersion> documentVersions) {
        List<TrainingDocument> trainingDocuments = new ArrayList<>();
        for (DocumentVersion documentVersion : documentVersions) {
            TrainingDocument trainingDocument = new TrainingDocument();
            trainingDocument.setTrainingInstance(trainingInstance);
            trainingDocument.setDocumentVersion(documentVersion);
            trainingDocuments.add(trainingDocument);
        }
        trainingDocumentRepository.saveAll(trainingDocuments);
    }






    // Find all


    // Find by id





}
