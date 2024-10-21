package maeilmail.admin;

import java.util.List;
import lombok.RequiredArgsConstructor;
import maeilmail.question.QuestionQueryService;
import maeilmail.question.QuestionSummary;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
class AdminViewController {

    private final QuestionQueryService questionQueryService;

    @GetMapping("/admin")
    public String index(Model model) {
        List<QuestionSummary> questions = questionQueryService.queryAllByCategory("all");
        model.addAttribute("questions", questions);

        return "admin/index";
    }
}
