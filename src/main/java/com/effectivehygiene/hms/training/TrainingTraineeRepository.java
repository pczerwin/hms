package com.effectivehygiene.hms.training;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TrainingTraineeRepository extends JpaRepository<TrainingTrainee, Long> {

    List<TrainingTrainee> findByTrainingInstanceId(Long trainingInstanceId);

    List<TrainingTrainee> findByTrainingInstanceIdIn(Collection<Long> trainingInstanceIds);
}
