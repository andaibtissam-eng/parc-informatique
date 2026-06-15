package com.parcinformatique.app.entity;

import com.parcinformatique.app.enums.AssignmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "assignments")
public class Assignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beneficiary_id", nullable = false)
    private User beneficiary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_id", nullable = false)
    private User assignedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validated_by_id")
    private User validatedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AssignmentStatus status = AssignmentStatus.PENDING;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate expectedReturnDate;

    private LocalDate endDate;

    @Column(nullable = false)
    private boolean approved = false;

    @Column(length = 255)
    private String digitalSignature;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToOne(mappedBy = "assignment")
    private EquipmentReturn returnRecord;
}
