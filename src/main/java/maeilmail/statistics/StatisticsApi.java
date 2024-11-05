package maeilmail.statistics;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class StatisticsApi {

    private final StatisticsService statisticsService;

    @GetMapping("/statistics/subscribe")
    public ResponseEntity<SubscribeReport> getDailySubscribeReport() {
        SubscribeReport report = statisticsService.generateDailySubscribeReport();

        return ResponseEntity.ok(report);
    }
}