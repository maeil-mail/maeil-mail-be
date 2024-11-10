package maeilmail.admin;

import java.util.List;
import lombok.RequiredArgsConstructor;
import maeilmail.question.QuestionQueryService;
import maeilmail.question.QuestionSummary;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
class AdminController {

    private final QuestionQueryService questionQueryService;
    private final AdminQuestionService adminQuestionService;

    @GetMapping("/admin")
    public String index(Model model) {
        List<QuestionSummary> questions = questionQueryService.queryAllByCategory("all");
        model.addAttribute("questions", questions);
        model.addAttribute("questionForm", new AdminQuestionForm());

        return "admin/index";
    }

    @PostMapping("/admin/question")
    public String postQuestion(@ModelAttribute("questionForm") AdminQuestionForm questionForm) {
        if (questionForm.isUpdate()) {
            adminQuestionService.updateQuestion(questionForm.toQuestion());
        } else {
            adminQuestionService.createQuestion(questionForm.toQuestion());
        }

        return "redirect:/admin";
    }

    @PutMapping("/admin/question")
    public ResponseEntity<Void> putQuestion(@RequestBody AdminQuestionRequest request) {
        if (request.isUpdate()) {
            adminQuestionService.updateQuestion(request.toQuestion());
        } else {
            adminQuestionService.createQuestion(request.toQuestion());
        }

        return ResponseEntity.noContent().build();
    }
}
