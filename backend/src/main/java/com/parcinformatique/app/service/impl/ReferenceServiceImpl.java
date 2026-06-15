package com.parcinformatique.app.service.impl;

import com.parcinformatique.app.constants.RoleCodes;
import com.parcinformatique.app.dto.common.ReferenceDataDto;
import com.parcinformatique.app.dto.common.ReferenceOptionDto;
import com.parcinformatique.app.repository.CategoryRepository;
import com.parcinformatique.app.repository.DepartmentRepository;
import com.parcinformatique.app.repository.LocationRepository;
import com.parcinformatique.app.repository.RoleRepository;
import com.parcinformatique.app.repository.SupplierRepository;
import com.parcinformatique.app.repository.UserRepository;
import com.parcinformatique.app.service.ReferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReferenceServiceImpl implements ReferenceService {

    private final DepartmentRepository departmentRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public ReferenceDataDto getReferenceData() {
        return new ReferenceDataDto(
            departmentRepository.findAllByOrderByNameAsc().stream()
                .map(item -> new ReferenceOptionDto(item.getId(), item.getName(), item.getCode()))
                .toList(),
            categoryRepository.findAllByOrderByNameAsc().stream()
                .map(item -> new ReferenceOptionDto(item.getId(), item.getName(), item.getCode()))
                .toList(),
            locationRepository.findAllByOrderByNameAsc().stream()
                .map(item -> new ReferenceOptionDto(item.getId(), item.getName(), item.getCode()))
                .toList(),
            supplierRepository.findAllByOrderByNameAsc().stream()
                .map(item -> new ReferenceOptionDto(item.getId(), item.getName(), item.getContactName()))
                .toList(),
            userRepository.findEligibleUsersByRole(RoleCodes.EMPLOYE).stream()
                .map(item -> new ReferenceOptionDto(item.getId(), item.getFirstName() + " " + item.getLastName(), item.getEmail()))
                .toList(),
            userRepository.findEligibleUsersByRole(RoleCodes.TECHNICIEN).stream()
                .map(item -> new ReferenceOptionDto(item.getId(), item.getFirstName() + " " + item.getLastName(), item.getEmail()))
                .toList(),
            roleRepository.findAllByOrderByNameAsc().stream().map(role -> role.getName()).toList()
        );
    }
}
