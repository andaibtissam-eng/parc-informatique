package com.parcinformatique.app.service;

import com.parcinformatique.app.dto.dashboard.DashboardSummaryDto;
import java.util.UUID;

public interface DashboardService {

    DashboardSummaryDto getSummary(UUID userId);
}
