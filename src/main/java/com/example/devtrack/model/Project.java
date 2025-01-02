package com.example.devtrack.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "projects")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "project_type", nullable = false)
    private String projectType;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "client_name", nullable = false)
    private String clientName;

    @Column(name = "total_price", nullable = false)
    @Min(value = 0)
    private double totalPrice;

    @Column(name = "advance_payment")
    @Min(value = 0)
    private double advancePayment;

    @Column(name = "deadline")
    private LocalDate deadline;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    public enum Status {
        ACTIVE("В розробці"),
        COMPLETED("Завершено"),
        CANCELLED("Скасовано");

        private final String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
