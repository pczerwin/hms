package com.effectivehygiene.hms.training;

import com.effectivehygiene.hms.training.dto.CreateTrainingInstanceRequest;
import com.effectivehygiene.hms.training.dto.TrainingInstanceMultiResponse;
import com.effectivehygiene.hms.training.dto.TrainingInstanceSingleResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/training/instances")
public class TrainingController {

	private final TrainingService trainingService;

	public TrainingController(TrainingService trainingService) {
		this.trainingService = trainingService;
	}

	@PostMapping
	public ResponseEntity<TrainingInstanceSingleResponse> createTrainingInstance(
			@Valid @RequestBody CreateTrainingInstanceRequest request
	) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(trainingService.createTrainingInstance(request));
	}

	@GetMapping
	public ResponseEntity<List<TrainingInstanceMultiResponse>> findAll() {
		return ResponseEntity.ok(trainingService.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<TrainingInstanceSingleResponse> findById(@PathVariable Long id) {
		return ResponseEntity.ok(trainingService.findByTrainingInstanceId(id));
	}
}
