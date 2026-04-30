package com.sep.realvista.application.service;

import com.sep.realvista.application.notification.dto.TemplateSchemaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class TemplateEngineService {

    public record RenderedTemplate(String title, String body) { }

    public RenderedTemplate preview(String titleTemplate, String bodyTemplate, Map<String, Object> variables) {
        String renderedTitle = replaceVariables(titleTemplate, variables);
        String renderedBody = replaceVariables(bodyTemplate, variables);
        return new RenderedTemplate(renderedTitle, renderedBody);
    }

    public TemplateSchemaResponse getSchema(String templateKey) {
        // Return some common variables for now
        List<TemplateSchemaResponse.VariableDefinition> variables = new ArrayList<>();
        variables.add(new TemplateSchemaResponse.VariableDefinition("name", "User's full name", true));
        variables.add(new TemplateSchemaResponse.VariableDefinition("otp", "One-time password", false));
        variables.add(new TemplateSchemaResponse.VariableDefinition("listingTitle", "Title of the listing", false));

        return TemplateSchemaResponse.builder()
                .variables(variables)
                .build();
    }

    private String replaceVariables(String template, Map<String, Object> variables) {
        if (template == null) {
            return "";
        }
        if (variables == null || variables.isEmpty()) {
            return template;
        }

        StringBuilder sb = new StringBuilder();
        // Support both {{var}} and ${var} or $${var}
        Pattern pattern = Pattern.compile("\\{\\{(.+?)\\}\\}|\\$?\\$\\{(.+?)\\}");
        Matcher matcher = pattern.matcher(template);

        int lastEnd = 0;
        while (matcher.find()) {
            sb.append(template, lastEnd, matcher.start());
            String key = matcher.group(1) != null ? matcher.group(1).trim() : matcher.group(2).trim();
            Object value = variables.get(key);
            sb.append(value != null ? value.toString() : matcher.group(0));
            lastEnd = matcher.end();
        }
        sb.append(template.substring(lastEnd));

        return sb.toString();
    }
}
