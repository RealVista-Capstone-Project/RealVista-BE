package com.sep.realvista.application.service;

import com.sep.realvista.application.notification.dto.TemplateSchemaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateEngineService {

    public record RenderedTemplate(String title, String body) { }

    public RenderedTemplate preview(String titleTemplate, String bodyTemplate, Map<String, Object> variables) {
        String renderedTitle = replaceVariables(titleTemplate, variables);
        String renderedBody = replaceVariables(bodyTemplate, variables);
        return new RenderedTemplate(renderedTitle, renderedBody);
    }

    public TemplateSchemaResponse getSchema(String templateKey) {
        List<TemplateSchemaResponse.VariableDefinition> variables = new ArrayList<>();
        
        switch (templateKey) {
            case "EMAIL_OTP" -> {
                variables.add(new TemplateSchemaResponse.VariableDefinition("userName", "User's full name", true));
                variables.add(new TemplateSchemaResponse.VariableDefinition("otp", "One-time password code", true));
                variables.add(new TemplateSchemaResponse.VariableDefinition(
                        "expiryMinutes", "Expiration time in minutes", true));
            }
            case "AGENT_PROPOSAL_NOTIFICATION" -> {
                variables.add(new TemplateSchemaResponse.VariableDefinition(
                        "ownerName", "Property owner's name", true));
                variables.add(new TemplateSchemaResponse.VariableDefinition("agentName", "Agent's name", true));
                variables.add(new TemplateSchemaResponse.VariableDefinition(
                        "proposalTitle", "Title of the proposal", true));
                variables.add(new TemplateSchemaResponse.VariableDefinition(
                        "propertyAddress", "Address of the property", true));
                variables.add(new TemplateSchemaResponse.VariableDefinition(
                        "viewEngagementsUrl", "Link to view the engagement", true));
            }
            case "AGENT_PROPOSAL_DECISION" -> {
                variables.add(new TemplateSchemaResponse.VariableDefinition("agentName", "Agent's name", true));
                variables.add(new TemplateSchemaResponse.VariableDefinition("ownerName", "Owner's name", true));
                variables.add(new TemplateSchemaResponse.VariableDefinition(
                        "proposalTitle", "Title of the proposal", true));
                variables.add(new TemplateSchemaResponse.VariableDefinition(
                        "propertyAddress", "Address of the property", true));
                variables.add(new TemplateSchemaResponse.VariableDefinition(
                        "accepted", "Boolean status of decision", true));
                variables.add(new TemplateSchemaResponse.VariableDefinition(
                        "viewEngagementsUrl", "Link to view the engagement", true));
            }
            case "TOUR_BOOKING_CONFIRMATION", "TOUR_BOOKING_NOTIFICATION", "TOUR_BOOKING_STATUS_CHANGE",
                 "TOUR_BOOKING_SUCCESS", "NEW_TOUR_REQUEST" -> {
                variables.add(new TemplateSchemaResponse.VariableDefinition(
                        "senderName", "Person who booked the tour", false));
                variables.add(new TemplateSchemaResponse.VariableDefinition(
                        "ownerName", "Property owner's name", false));
                variables.add(new TemplateSchemaResponse.VariableDefinition("listingName", "Listing title", false));
                variables.add(new TemplateSchemaResponse.VariableDefinition(
                        "propertyAddress", "Property address", false));
                variables.add(new TemplateSchemaResponse.VariableDefinition("tourDate", "Date of the tour", false));
                variables.add(new TemplateSchemaResponse.VariableDefinition(
                        "tourTime", "Time slot of the tour", false));
                variables.add(new TemplateSchemaResponse.VariableDefinition(
                        "status", "Current status of booking", false));
                variables.add(new TemplateSchemaResponse.VariableDefinition("notes", "Sender notes", false));
                variables.add(new TemplateSchemaResponse.VariableDefinition(
                        "viewAppointmentUrl", "Link to view appointments", false));
            }
            case "LEASE_TERMINATED" -> {
                variables.add(new TemplateSchemaResponse.VariableDefinition("reason", "Reason for termination", true));
            }
            case "APPOINTMENT_ACCEPTED", "APPOINTMENT_REJECTED", "APPOINTMENT_CANCELLED",
                 "APPOINTMENT_COMPLETED" -> {
                variables.add(new TemplateSchemaResponse.VariableDefinition("listingName", "Listing title", true));
                variables.add(new TemplateSchemaResponse.VariableDefinition("tourDate", "Date of the tour", true));
                variables.add(new TemplateSchemaResponse.VariableDefinition(
                        "tourTime", "Time slot of the tour", true));
                if (templateKey.equals("APPOINTMENT_REJECTED") || templateKey.equals("APPOINTMENT_CANCELLED")) {
                    variables.add(new TemplateSchemaResponse.VariableDefinition(
                            "reason", "Reason for rejection/cancellation", true));
                }
                if (templateKey.equals("APPOINTMENT_CANCELLED")) {
                    variables.add(new TemplateSchemaResponse.VariableDefinition(
                            "actorName", "Person who cancelled", true));
                }
            }
            case "SYSTEM" -> {
                variables.add(new TemplateSchemaResponse.VariableDefinition(
                        "message", "The system message content", true));
            }
            default -> { }
        }

        return TemplateSchemaResponse.builder()
                .variables(variables)
                .build();
    }

    private String replaceVariables(String template, Map<String, Object> variables) {
        if (template == null) {
            return "";
        }
        if (variables == null) {
            variables = Map.of();
        }

        StringBuilder sb = new StringBuilder();
        // Support both {{var}} and ${var}
        Pattern pattern = Pattern.compile("\\{\\{(.+?)\\}\\}|\\$?\\$\\{(.+?)\\}");
        Matcher matcher = pattern.matcher(template);

        int lastEnd = 0;
        while (matcher.find()) {
            sb.append(template, lastEnd, matcher.start());
            String key = matcher.group(1) != null ? matcher.group(1).trim() : matcher.group(2).trim();
            Object value = variables.get(key);
            
            if (value != null) {
                sb.append(value.toString());
            } else {
                // If variable missing, keep the placeholder for debugging in dev, 
                // but in production we might want to log a warning.
                log.warn("Missing variable '{}' for template rendering", key);
                sb.append(matcher.group(0)); 
            }
            lastEnd = matcher.end();
        }
        sb.append(template.substring(lastEnd));

        return sb.toString();
    }

    /**
     * Validates if the template contains all required variables.
     * This should be called before saving a template in the Admin Service.
     */
    public List<String> validateRequiredVariables(String templateKey, String content) {
        log.info("Validating template variables for key: {}. Content length: {}", 
                templateKey, content != null ? content.length() : 0);
        List<String> required = getRequiredVariablesFor(templateKey);
        List<String> missing = new ArrayList<>();
        
        if (content == null || content.isBlank()) {
            return required;
        }

        for (String var : required) {
            // Even more flexible regex: just look for the variable name surrounded by brackets, 
            // ignoring any potential invisible characters or slightly different formatting.
            // We search for the pattern {{...var...}} or ${...var...}
            String regex = "(\\{\\{|\\$\\{)[^}]*?" + Pattern.quote(var) + "[^}]*?(\\}\\}|\\})";
            Pattern pattern = Pattern.compile(regex);
            boolean found = pattern.matcher(content).find();
            
            log.info("Checking for variable '{}' in content. Found: {}", var, found);
            
            if (!found) {
                missing.add(var);
            }
        }
        
        if (!missing.isEmpty()) {
            log.warn("Template {} is missing required variables: {}", templateKey, missing);
        }
        return missing;
    }

    private List<String> getRequiredVariablesFor(String templateKey) {
        return switch (templateKey) {
            case "EMAIL_OTP" -> List.of("otp", "expiryMinutes");
            case "AGENT_PROPOSAL_NOTIFICATION" -> List.of("agentName", "ownerName");
            case "TOUR_BOOKING_CONFIRMATION", "TOUR_BOOKING_SUCCESS" -> List.of();
            case "NEW_TOUR_REQUEST" -> List.of("senderName", "listingName");
            case "LEASE_TERMINATED" -> List.of("reason");
            case "APPOINTMENT_REJECTED", "APPOINTMENT_CANCELLED" -> List.of("listingName", "reason");
            case "SYSTEM" -> List.of("message");
            default -> List.of();
        };
    }
}
