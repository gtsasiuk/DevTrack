package com.example.devtrack.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private Long id;

    @Column(name = "project_type", nullable = false)
    private String projectType;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "client_name", nullable = false)
    private String clientName;

    @Column(name = "total_price", nullable = false)
    @Min(value = 0)
    @Builder.Default
    private Long totalPrice = 0L;

    @Column(name = "advance_payment")
    @Min(value = 0)
    @Builder.Default
    private Long advancePayment = 0L;

    @Column(name = "creation_date", updatable = false)
    @CreationTimestamp
    private LocalDateTime creationDate;

    @Column(name = "deadline")
    @Builder.Default
    private LocalDate deadline = LocalDate.now();

    @Column(name = "project_link")
    @Builder.Default
    private String projectLink = "";

    @Column(name = "description")
    @Builder.Default
    private String description = "";

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    public enum Status {
        ACTIVE("project.status.active"),
        COMPLETED("project.status.completed"),
        CANCELLED("project.status.cancelled"),
        EXPIRED("project.status.expired");

        private final String messageKey;

        Status(String messageKey) {
            this.messageKey = messageKey;
        }

        public String getMessageKey() {
            return messageKey;
        }
    }
}
