package com.lmoving.intranet.activity;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import com.lmoving.intranet.activity.dto.OperationActivityResponse;

@RestController
@RequestMapping("/api/operations/{operationId}/activity")
public class OperationActivityController {
    private final OperationActivityService activityService;
    public OperationActivityController(OperationActivityService activityService) { this.activityService = activityService; }
    @GetMapping
    @PreAuthorize("hasAuthority('OPERATIONS.ACTIVITY_VIEW')")
    public List<OperationActivityResponse> listar(@PathVariable Long operationId) {
        return activityService.listar(operationId);
    }
}
