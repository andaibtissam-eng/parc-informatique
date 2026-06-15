package com.parcinformatique.app.repository;

import com.parcinformatique.app.entity.Ticket;
import com.parcinformatique.app.enums.TicketStatus;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    long countByStatus(TicketStatus status);
}
