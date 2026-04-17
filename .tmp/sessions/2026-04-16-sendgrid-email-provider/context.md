# Task Context: Add SendGrid Email Provider

Session ID: 2026-04-16-sendgrid-email-provider
Created: 2026-04-16T00:00:00Z
Status: in_progress

## Current Request
Add SendGrid as a second email provider alongside existing SMTP (Gmail) provider, using Strategy/Provider pattern with config-based switching.

## Context Files (Standards to Follow)
- /home/nguyenhcp/.opencode/context/core/standards/code-quality.md
- /home/nguyenhcp/.opencode/context/core/essential-patterns.md
- /home/nguyenhcp/.opencode/context/core/standards/security-patterns.md

## Reference Files (Source Material)
- src/main/java/com/sep/realvista/application/service/EmailService.java
- src/main/java/com/sep/realvista/infrastructure/external/email/EmailServiceImpl.java
- src/test/java/com/sep/realvista/infrastructure/external/email/EmailServiceImplTest.java
- src/main/resources/application.yml
- pom.xml

## Architecture
EmailService (interface)
  └── EmailServiceImpl  ← delegates to EmailProvider
          └── EmailProvider (new interface)
                  ├── SmtpEmailProvider    ← extracts current JavaMailSender logic
                  └── SendGridEmailProvider ← new, uses sendgrid-java SDK

## Components
1. EmailProvider interface - sendHtml(to, subject, htmlBody) + sendSimple(to, subject, text)
2. SmtpEmailProvider - extract existing SMTP logic, @ConditionalOnProperty(email.provider=smtp)
3. SendGridEmailProvider - new SendGrid HTTP API provider, @ConditionalOnProperty(email.provider=sendgrid)
4. EmailServiceImpl refactor - delegate send calls to injected EmailProvider
5. Config - email.provider: smtp (default), sendgrid.api-key env var

## Constraints
- Java / Spring Boot project
- Keep EmailService interface unchanged (backward compatible)
- Thymeleaf template engine stays in EmailServiceImpl (not provider concern)
- API key via env var SENDGRID_API_KEY, never hardcoded
- sendgrid-java SDK (com.sendgrid:sendgrid-java)

## Exit Criteria
- [ ] EmailProvider interface created
- [ ] SmtpEmailProvider created with existing SMTP logic
- [ ] SendGridEmailProvider created using sendgrid-java
- [ ] EmailServiceImpl updated to delegate to EmailProvider
- [ ] pom.xml has sendgrid-java dependency
- [ ] application.yml has email.provider config
- [ ] Tests updated/created
