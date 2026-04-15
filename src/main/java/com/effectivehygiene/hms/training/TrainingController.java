package com.effectivehygiene.hms.training;

import com.effectivehygiene.hms.training.dto.CreateTrainingInstanceRequest;
import com.effectivehygiene.hms.training.dto.TrainingInstanceResponse;
import com.effectivehygiene.hms.training.dto.TrainingMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/training/instances")
public class TrainingController {

	private final TrainingService trainingService;

	public TrainingController(TrainingService trainingService) {
		this.trainingService = trainingService;
	}

	@PostMapping
	public ResponseEntity<TrainingInstanceResponse> createTrainingInstance(
			@Valid @RequestBody CreateTrainingInstanceRequest request
	) {
		TrainingInstance created = trainingService.createTrainingInstance(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(TrainingMapper.toResponse(created));
	}
}
