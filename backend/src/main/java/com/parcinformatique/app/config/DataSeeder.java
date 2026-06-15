package com.parcinformatique.app.config;

import com.parcinformatique.app.constants.PermissionCodes;
import com.parcinformatique.app.constants.RoleCodes;
import com.parcinformatique.app.entity.Category;
import com.parcinformatique.app.entity.Department;
import com.parcinformatique.app.entity.Equipment;
import com.parcinformatique.app.entity.Location;
import com.parcinformatique.app.entity.Permission;
import com.parcinformatique.app.entity.Role;
import com.parcinformatique.app.entity.Supplier;
import com.parcinformatique.app.entity.User;
import com.parcinformatique.app.enums.EquipmentStatus;
import com.parcinformatique.app.enums.UserStatus;
import com.parcinformatique.app.repository.CategoryRepository;
import com.parcinformatique.app.repository.DepartmentRepository;
import com.parcinformatique.app.repository.EquipmentRepository;
import com.parcinformatique.app.repository.LocationRepository;
import com.parcinformatique.app.repository.PermissionRepository;
import com.parcinformatique.app.repository.RoleRepository;
import com.parcinformatique.app.repository.SupplierRepository;
import com.parcinformatique.app.repository.UserRepository;
import com.parcinformatique.app.utils.CodeGeneratorUtil;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private static final String DEFAULT_ADMIN_EMAIL = "admin@emsi.ma";
    private static final String LEGACY_ADMIN_EMAIL = "admin@parc.local";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin123*";

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final LocationRepository locationRepository;
    private final SupplierRepository supplierRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final EquipmentRepository equipmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final CodeGeneratorUtil codeGeneratorUtil;

    @Bean
    CommandLineRunner seedData() {
        return args -> {
            Map<String, Permission> permissions = ensurePermissions();
            Map<String, Role> roles = ensureRoles(permissions);

            Department itDepartment = ensureDepartment();
            Location hq = ensureLocation(itDepartment);
            Supplier supplier = ensureSupplier();
            Category laptop = ensureCategory();

            User mainAdmin = ensureMainAdministrator(roles.get(RoleCodes.ADMINISTRATOR), itDepartment);
            normalizeAdditionalAdministrators(mainAdmin, roles.get(RoleCodes.RESPONSABLE_INFORMATIQUE));

            seedSampleInventoryIfEmpty(itDepartment, hq, supplier, laptop);
        };
    }

    private Map<String, Permission> ensurePermissions() {
        Map<String, Permission> permissions = new LinkedHashMap<>();
        permissions.put(PermissionCodes.DASHBOARD_READ, upsertPermission(PermissionCodes.DASHBOARD_READ, "Acces dashboard"));
        permissions.put(PermissionCodes.ANALYTICS_READ, upsertPermission(PermissionCodes.ANALYTICS_READ, "Consultation analytics"));
        permissions.put(PermissionCodes.USERS_MANAGE, upsertPermission(PermissionCodes.USERS_MANAGE, "Gestion des utilisateurs"));
        permissions.put(PermissionCodes.ROLES_MANAGE, upsertPermission(PermissionCodes.ROLES_MANAGE, "Gestion des roles"));
        permissions.put(PermissionCodes.PERMISSIONS_MANAGE, upsertPermission(PermissionCodes.PERMISSIONS_MANAGE, "Gestion des permissions"));
        permissions.put(PermissionCodes.SYSTEM_MANAGE, upsertPermission(PermissionCodes.SYSTEM_MANAGE, "Parametrage systeme"));
        permissions.put(PermissionCodes.AUDIT_READ, upsertPermission(PermissionCodes.AUDIT_READ, "Consultation des audits"));
        permissions.put(PermissionCodes.EQUIPMENTS_READ, upsertPermission(PermissionCodes.EQUIPMENTS_READ, "Lecture des equipements"));
        permissions.put(PermissionCodes.EQUIPMENTS_MANAGE, upsertPermission(PermissionCodes.EQUIPMENTS_MANAGE, "Gestion des equipements"));
        permissions.put(PermissionCodes.ASSIGNMENTS_MANAGE, upsertPermission(PermissionCodes.ASSIGNMENTS_MANAGE, "Gestion des affectations"));
        permissions.put(PermissionCodes.ASSIGNMENTS_READ_OWN, upsertPermission(PermissionCodes.ASSIGNMENTS_READ_OWN, "Lecture de ses affectations"));
        permissions.put(PermissionCodes.REPORTS_READ, upsertPermission(PermissionCodes.REPORTS_READ, "Consultation des rapports"));
        permissions.put(PermissionCodes.MAINTENANCES_READ, upsertPermission(PermissionCodes.MAINTENANCES_READ, "Lecture maintenance"));
        permissions.put(PermissionCodes.MAINTENANCES_MANAGE, upsertPermission(PermissionCodes.MAINTENANCES_MANAGE, "Gestion maintenance"));
        permissions.put(PermissionCodes.MAINTENANCES_REQUEST, upsertPermission(PermissionCodes.MAINTENANCES_REQUEST, "Demande maintenance"));
        permissions.put(PermissionCodes.NOTIFICATIONS_READ_OWN, upsertPermission(PermissionCodes.NOTIFICATIONS_READ_OWN, "Lecture de ses notifications"));
        permissions.put(PermissionCodes.PROFILE_MANAGE_OWN, upsertPermission(PermissionCodes.PROFILE_MANAGE_OWN, "Gestion de son profil"));
        permissions.put(PermissionCodes.REFERENCES_READ, upsertPermission(PermissionCodes.REFERENCES_READ, "Lecture des referentiels"));
        return permissions;
    }

    private Map<String, Role> ensureRoles(Map<String, Permission> permissions) {
        Map<String, Role> roles = new LinkedHashMap<>();
        roles.put(
            RoleCodes.ADMINISTRATOR,
            upsertRole(
                RoleCodes.ADMINISTRATOR,
                List.of("ADMIN"),
                "Administrateur principal du systeme",
                Set.copyOf(permissions.values())
            )
        );
        roles.put(
            RoleCodes.RESPONSABLE_INFORMATIQUE,
            upsertRole(
                RoleCodes.RESPONSABLE_INFORMATIQUE,
                List.of("GESTIONNAIRE"),
                "Responsable du parc informatique",
                Set.of(
                    permissions.get(PermissionCodes.DASHBOARD_READ),
                    permissions.get(PermissionCodes.EQUIPMENTS_READ),
                    permissions.get(PermissionCodes.EQUIPMENTS_MANAGE),
                    permissions.get(PermissionCodes.ASSIGNMENTS_MANAGE),
                    permissions.get(PermissionCodes.REPORTS_READ),
                    permissions.get(PermissionCodes.MAINTENANCES_READ),
                    permissions.get(PermissionCodes.NOTIFICATIONS_READ_OWN),
                    permissions.get(PermissionCodes.PROFILE_MANAGE_OWN),
                    permissions.get(PermissionCodes.REFERENCES_READ)
                )
            )
        );
        roles.put(
            RoleCodes.TECHNICIEN,
            upsertRole(
                RoleCodes.TECHNICIEN,
                List.of(),
                "Technicien de maintenance",
                Set.of(
                    permissions.get(PermissionCodes.DASHBOARD_READ),
                    permissions.get(PermissionCodes.MAINTENANCES_READ),
                    permissions.get(PermissionCodes.MAINTENANCES_MANAGE),
                    permissions.get(PermissionCodes.NOTIFICATIONS_READ_OWN),
                    permissions.get(PermissionCodes.PROFILE_MANAGE_OWN),
                    permissions.get(PermissionCodes.REFERENCES_READ)
                )
            )
        );
        roles.put(
            RoleCodes.EMPLOYE,
            upsertRole(
                RoleCodes.EMPLOYE,
                List.of("BENEFICIAIRE"),
                "Employe consommateur du service",
                Set.of(
                    permissions.get(PermissionCodes.DASHBOARD_READ),
                    permissions.get(PermissionCodes.ASSIGNMENTS_READ_OWN),
                    permissions.get(PermissionCodes.MAINTENANCES_REQUEST),
                    permissions.get(PermissionCodes.NOTIFICATIONS_READ_OWN),
                    permissions.get(PermissionCodes.PROFILE_MANAGE_OWN)
                )
            )
        );
        return roles;
    }

    private Department ensureDepartment() {
        return departmentRepository.findByCode("DSI").orElseGet(() -> {
            Department department = new Department();
            department.setName("Direction des Systemes d'Information");
            department.setCode("DSI");
            department.setDescription("Pilotage du parc et support");
            return departmentRepository.save(department);
        });
    }

    private Location ensureLocation(Department department) {
        return locationRepository.findAllByOrderByNameAsc().stream()
            .filter(location -> "HQ-SRV".equalsIgnoreCase(location.getCode()))
            .findFirst()
            .orElseGet(() -> {
                Location location = new Location();
                location.setName("Siege - Salle Serveurs");
                location.setCode("HQ-SRV");
                location.setBuilding("Tour A");
                location.setFloor("3");
                location.setRoom("303");
                location.setDepartment(department);
                return locationRepository.save(location);
            });
    }

    private Supplier ensureSupplier() {
        return supplierRepository.findAllByOrderByNameAsc().stream()
            .filter(supplier -> "Tech Source Maroc".equalsIgnoreCase(supplier.getName()))
            .findFirst()
            .orElseGet(() -> {
                Supplier supplier = new Supplier();
                supplier.setName("Tech Source Maroc");
                supplier.setContactName("Salma IT");
                supplier.setEmail("contact@techsource.ma");
                supplier.setPhone("+212600000000");
                return supplierRepository.save(supplier);
            });
    }

    private Category ensureCategory() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
            .filter(category -> "LAPTOP".equalsIgnoreCase(category.getCode()))
            .findFirst()
            .orElseGet(() -> {
                Category category = new Category();
                category.setName("Ordinateurs portables");
                category.setCode("LAPTOP");
                category.setDescription("Postes mobiles et premium");
                return categoryRepository.save(category);
            });
    }

    private User ensureMainAdministrator(Role adminRole, Department department) {
        User adminUser = userRepository.findBySystemAccountTrue()
            .or(() -> userRepository.findByEmailIgnoreCase(DEFAULT_ADMIN_EMAIL))
            .or(() -> userRepository.findByEmailIgnoreCase(LEGACY_ADMIN_EMAIL))
            .orElseGet(User::new);

        boolean shouldResetCredentials = adminUser.getId() == null || LEGACY_ADMIN_EMAIL.equalsIgnoreCase(adminUser.getEmail());

        adminUser.setFirstName("Admin");
        adminUser.setLastName("Principal");
        adminUser.setEmail(DEFAULT_ADMIN_EMAIL);
        adminUser.setDepartment(department);
        adminUser.setEmailVerified(true);
        adminUser.setEnabled(true);
        adminUser.setSystemAccount(true);
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser.setRoles(Set.of(adminRole));
        adminUser.setFailedLoginAttempts(0);
        if (shouldResetCredentials || adminUser.getPasswordHash() == null || adminUser.getPasswordHash().isBlank()) {
            adminUser.setPasswordHash(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
        }

        return userRepository.save(adminUser);
    }

    private void normalizeAdditionalAdministrators(User mainAdmin, Role fallbackRole) {
        List<User> administrators = new ArrayList<>(userRepository.findByRoles_Name(RoleCodes.ADMINISTRATOR));
        for (User administrator : administrators) {
            if (administrator.getId().equals(mainAdmin.getId())) {
                continue;
            }
            administrator.setSystemAccount(false);
            administrator.setRoles(Set.of(fallbackRole));
            if (administrator.getStatus() == UserStatus.LOCKED || administrator.getStatus() == UserStatus.REJECTED) {
                administrator.setEnabled(false);
            }
            userRepository.save(administrator);
        }
    }

    private void seedSampleInventoryIfEmpty(Department department, Location location, Supplier supplier, Category category) {
        if (equipmentRepository.count() > 0) {
            return;
        }

        Equipment equipment = new Equipment();
        equipment.setInventoryCode(codeGeneratorUtil.inventoryCode());
        equipment.setName("MacBook Pro M3");
        equipment.setBrand("Apple");
        equipment.setModel("14 pouces");
        equipment.setImageUrl("https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=900&q=80");
        equipment.setQrCode("EQ-" + codeGeneratorUtil.reference("QR"));
        equipment.setStatus(EquipmentStatus.AVAILABLE);
        equipment.setCategory(category);
        equipment.setSupplier(supplier);
        equipment.setLocation(location);
        equipment.setDepartment(department);
        equipment.setPurchaseDate(LocalDate.now().minusMonths(2));
        equipment.setWarrantyEndDate(LocalDate.now().plusYears(2));
        equipmentRepository.save(equipment);

        Equipment spareEquipment = new Equipment();
        spareEquipment.setInventoryCode(codeGeneratorUtil.inventoryCode());
        spareEquipment.setName("Dell Latitude 7450");
        spareEquipment.setBrand("Dell");
        spareEquipment.setModel("Latitude");
        spareEquipment.setImageUrl("https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?auto=format&fit=crop&w=900&q=80");
        spareEquipment.setQrCode("EQ-" + codeGeneratorUtil.reference("QR"));
        spareEquipment.setStatus(EquipmentStatus.AVAILABLE);
        spareEquipment.setCategory(category);
        spareEquipment.setSupplier(supplier);
        spareEquipment.setLocation(location);
        spareEquipment.setDepartment(department);
        spareEquipment.setPurchaseDate(LocalDate.now().minusMonths(6));
        spareEquipment.setWarrantyEndDate(LocalDate.now().plusYears(1));
        equipmentRepository.save(spareEquipment);
    }

    private Permission upsertPermission(String code, String label) {
        Optional<Permission> existing = permissionRepository.findByCodeIn(List.of(code)).stream().findFirst();
        Permission permission = existing.orElseGet(Permission::new);
        permission.setCode(code);
        permission.setLabel(label);
        permission.setDescription(label);
        return permissionRepository.save(permission);
    }

    private Role upsertRole(String canonicalName, List<String> legacyNames, String description, Set<Permission> permissions) {
        Role role = roleRepository.findByName(canonicalName)
            .or(() -> legacyNames.stream().map(roleRepository::findByName).flatMap(Optional::stream).findFirst())
            .orElseGet(Role::new);
        role.setName(canonicalName);
        role.setDescription(description);
        role.setSystemRole(true);
        role.setPermissions(permissions);
        return roleRepository.save(role);
    }
}
