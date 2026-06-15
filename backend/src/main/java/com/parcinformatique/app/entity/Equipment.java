package com.parcinformatique.app.entity;

import com.parcinformatique.app.enums.EquipmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "equipments")
public class Equipment extends BaseEntity {

    @Column(nullable = false, unique = true, length = 40)
    private String inventoryCode;

    @Column(unique = true, length = 80)
    private String serialNumber;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 120)
    private String brand;

    @Column(length = 120)
    private String model;

    @Column(unique = true, length = 120)
    private String qrCode;

    @Column(length = 60)
    private String assetTag;

    @Column(length = 255)
    private String imageUrl;

    @Column(length = 255)
    private String documentUrl;

    @Column(length = 255)
    private String operatingSystem;

    private Integer memoryGb;

    private Integer storageGb;

    @Column(length = 255)
    private String processor;

    private LocalDate purchaseDate;

    private LocalDate warrantyEndDate;

    private BigDecimal acquisitionCost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EquipmentStatus status = EquipmentStatus.AVAILABLE;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "active_assignment_id")
    private Assignment activeAssignment;

    @OneToMany(mappedBy = "equipment")
    private List<Assignment> assignments = new ArrayList<>();

    @OneToMany(mappedBy = "equipment")
    private List<Maintenance> maintenances = new ArrayList<>();

    @OneToMany(mappedBy = "equipment")
    private List<Ticket> tickets = new ArrayList<>();
}
