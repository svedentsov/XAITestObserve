package com.svedentsov.xaiobserverapp.controller;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.service.DemoDataFactory;
import com.svedentsov.xaiobserverapp.service.TestEventOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DemoController {

    private final TestEventOrchestrator testEventOrchestrator;
    private final DemoDataFactory demoDataFactory;

    @PostMapping("/demo/create")
    public String createDemoTestRun(RedirectAttributes redirectAttributes) {
        log.info("Request to create a demo test run record.");
        try {
            FailureEventDTO event = demoDataFactory.generateRandomEvent();
            // Используем тот же асинхронный сервис
            testEventOrchestrator.processAndSaveTestEvent(event);

            redirectAttributes.addFlashAttribute("message",
                    String.format("Demo record '%s' with status '%s' is being processed!", event.getTestMethod(), event.getStatus()));
            log.info("Demo record for '{}' created and sent for processing.", event.getTestMethod());

        } catch (Exception e) {
            log.error("Error creating demo record:", e);
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/";
    }
}
