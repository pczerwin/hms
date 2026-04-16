package com.effectivehygiene.hms.training;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TrainingDocumentRepository extends JpaRepository<TrainingDocument, Long> {

    List<TrainingDocument> findByTrainingInstanceId(Long trainingInstanceId);

    List<TrainingDocument> findByTrainingInstanceIdIn(Collection<Long> trainingInstanceIds);
}
