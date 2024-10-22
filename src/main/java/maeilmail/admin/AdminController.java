package maeilmail.admin;

import java.util.List;
import lombok.RequiredArgsConstructor;
import maeilmail.question.QuestionQueryService;
import maeilmail.question.QuestionSummary;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

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

        return "/admin/index";
    }

    @PostMapping("/admin/question")
    public String putQuestion(@ModelAttribute("questionForm") AdminQuestionForm questionForm) {
        adminQuestionService.createQuestion(questionForm.toQuestion());

        return "redirect:/admin";
    }
}
