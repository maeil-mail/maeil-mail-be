package maeilmail.statistics;

import java.time.LocalDate;
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
        SubscribeReport report = statisticsService.generateSubscribeReport();

        return ResponseEntity.ok(report);
    }

    @GetMapping("/statistics/subscribe-question")
    public ResponseEntity<DailySendReport> getDailySubscribeQuestionReport() {
        DailySendReport report = statisticsService.generateDailySendReport(LocalDate.now());

        return ResponseEntity.ok(report);
    }
}
