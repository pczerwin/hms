package com.effectivehygiene.hms.training;

import com.effectivehygiene.hms.employee.Employee;
import jakarta.persistence.*;

@Entity
@Table(
        name = "training_trainee",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_training_employee",
                columnNames = {"training_instance_id", "employee_id"}
        )
)
public class TrainingTrainee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "training_instance_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_tt_training")
    )
    private TrainingInstance trainingInstance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "employee_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_tt_employee")
    )
    private Employee employee;

    public Long getId() {
        return id;
    }

    public TrainingInstance getTrainingInstance() {
        return trainingInstance;
    }

    public void setTrainingInstance(TrainingInstance trainingInstance) {
        this.trainingInstance = trainingInstance;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}
