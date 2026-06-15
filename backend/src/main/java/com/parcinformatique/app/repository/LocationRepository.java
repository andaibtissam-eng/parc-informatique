package com.parcinformatique.app.repository;

import com.parcinformatique.app.entity.Location;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, UUID> {

    List<Location> findAllByOrderByNameAsc();
}
