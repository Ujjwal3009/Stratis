package com.upsc.ai.controller;

import com.upsc.ai.dto.GlobalPerformanceDTO;
import com.upsc.ai.entity.User;
import com.upsc.ai.repository.UserRepository;
import com.upsc.ai.security.UserPrincipal;
import com.upsc.ai.service.BehaviourAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analysis")
@Tag(name = "Analysis", description = "Performance analysis and diagnostic endpoints")
public class AnalysisController {

    @Autowired
    private BehaviourAnalyticsService analysisService;

    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "Get overall performance", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/overall")
    public ResponseEntity<GlobalPerformanceDTO> getOverallPerformance(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(analysisService.calculateGlobalPerformance(user));
    }
}
