package com.parcinformatique.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "activity_logs")
public class ActivityLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Column(nullable = false, length = 120)
    private String verb;

    @Column(nullable = false, length = 120)
    private String subjectType;

    @Column(nullable = false, length = 80)
    private String subjectId;

    @Column(nullable = false, length = 300)
    private String description;

    @Column(length = 80)
    private String icon;

    @Column(length = 30)
    private String color;

    @Column(nullable = false)
    private LocalDateTime occurredAt = LocalDateTime.now();
}
