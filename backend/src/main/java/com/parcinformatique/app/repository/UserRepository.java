package com.parcinformatique.app.repository;

import com.parcinformatique.app.entity.User;
import com.parcinformatique.app.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = {"department", "roles", "roles.permissions"})
    Optional<User> findWithSecurityByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("""
        select distinct u from User u
        left join u.roles r
        where (
            :search is null or :search = '' or
            lower(u.firstName) like lower(concat('%', :search, '%')) or
            lower(u.lastName) like lower(concat('%', :search, '%')) or
            lower(u.email) like lower(concat('%', :search, '%'))
        )
        and (:status is null or u.status = :status)
        and (:roleName is null or r.name = :roleName)
        """)
    Page<User> searchUsers(
        @Param("search") String search,
        @Param("status") UserStatus status,
        @Param("roleName") String roleName,
        Pageable pageable
    );

    List<User> findByRoles_NameAndEnabledTrueOrderByFirstNameAscLastNameAsc(String roleName);

    @Query("""
        select distinct u from User u
        join u.roles r
        where r.name = :roleName
          and u.enabled = true
          and u.emailVerified = true
          and u.status = com.parcinformatique.app.enums.UserStatus.ACTIVE
        order by u.firstName asc, u.lastName asc
        """)
    List<User> findEligibleUsersByRole(@Param("roleName") String roleName);

    List<User> findByRoles_Name(String roleName);

    Optional<User> findBySystemAccountTrue();

    long countByStatus(UserStatus status);

    long countByEmailVerifiedTrue();

    long countByRoles_Name(String roleName);

    List<User> findTop6ByOrderByCreatedAtDesc();
}
