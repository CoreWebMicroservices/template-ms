# Template Management Service

> **Part of [Core Microservices Project](https://github.com/CoreWebMicroservices/corems-project)** - Enterprise-grade microservices toolkit for rapid application development

Centralized template management and rendering service using Handlebars templating engine. Provides CRUD operations for templates and server-side rendering with parameter validation.

## Features

### Template Management
- **CRUD Operations** - Create, read, update, delete templates
- **Soft Delete** - Templates are marked as deleted, not removed
- **Multi-language Support** - Templates can have language variants (default: 'en')
- **Category Organization** - EMAIL, SMS, DOCUMENT, REPORT, NOTIFICATION
- **Search & Filter** - Paginated listing with category filtering
- **Version Control** - Track creation and modification metadata

### Template Rendering
- **Handlebars Engine** - Industry-standard templating with {{variable}} syntax
- **Parameter Validation** - Required parameters extracted and validated
- **HTML Escaping** - Automatic XSS protection with {{variable}}
- **Safe HTML** - Unescaped rendering with {{{variable}}} when needed
- **Conditionals** - {{#if}}, {{#unless}} logic
- **Iteration** - {{#each}} loops for arrays
- **Nested Access** - {{user.address.city}} dot notation
- **Template Caching** - Compiled templates cached for performance

### Security
- **Role-Based Access** - Admin-only template management
- **JWT Authentication** - Secure API access
- **Input Validation** - Bean validation on all requests
- **XSS Protection** - HTML escaping by default

## Quick Start

```bash
# Clone the main project
git clone https://github.com/CoreWebMicroservices/corems-project.git
cd corems-project

# Start infrastructure (PostgreSQL)
./setup.sh infra

# Build and start template service
./setup.sh build template-ms
./setup.sh start template-ms

# Or start entire stack
./setup.sh start-all
```

## API Endpoints

**Base URL**: `http://localhost:3004`

### Template Management

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/api/templates` | POST | Admin | Create new template |
| `/api/templates` | GET | Yes | List templates (paginated) |
| `/api/templates/{templateId}` | GET | Yes | Get template by ID |
| `/api/templates/{templateId}` | PUT | Admin | Update template |
| `/api/templates/{templateId}` | DELETE | Admin | Soft delete template |
| `/api/templates/{templateId}/metadata` | GET | Yes | Get template metadata |

### Template Rendering

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/api/templates/{templateId}/render` | POST | Yes | Render template with parameters |

## Template Examples

### Welcome Email Template

```handlebars
<h1>Welcome, {{user.firstName}}!</h1>

<p>Thank you for joining {{companyName}}. We're excited to have you on board.</p>

{{#if user.emailVerified}}
  <p>Your email has been verified. You're all set!</p>
{{else}}
  <p>Please verify your email by clicking the link below:</p>
  <a href="{{verificationUrl}}">Verify Email</a>
{{/if}}

<p>Best regards,<br>The {{companyName}} Team</p>
```

**Required Parameters**: `user.firstName`, `companyName`, `user.emailVerified`, `verificationUrl`

### Invoice Template

```handlebars
<h1>Invoice #{{invoice.number}}</h1>

<p><strong>Date:</strong> {{invoice.date}}</p>
<p><strong>Due Date:</strong> {{invoice.dueDate}}</p>

<h2>Bill To:</h2>
<p>
  {{customer.name}}<br>
  {{customer.address.street}}<br>
  {{customer.address.city}}, {{customer.address.state}} {{customer.address.zip}}
</p>

<table>
  <thead>
    <tr>
      <th>Item</th>
      <th>Quantity</th>
      <th>Price</th>
      <th>Total</th>
    </tr>
  </thead>
  <tbody>
    {{#each invoice.items}}
    <tr>
      <td>{{this.description}}</td>
      <td>{{this.quantity}}</td>
      <td>${{this.price}}</td>
      <td>${{this.total}}</td>
    </tr>
    {{/each}}
  </tbody>
</table>

<p><strong>Total: ${{invoice.total}}</strong></p>
```

**Required Parameters**: `invoice.number`, `invoice.date`, `invoice.dueDate`, `customer.name`, `customer.address`, `invoice.items`, `invoice.total`

### SMS Notification Template

```handlebars
Hi {{user.firstName}}, your order #{{order.id}} has been {{order.status}}. Track it here: {{trackingUrl}}
```

**Required Parameters**: `user.firstName`, `order.id`, `order.status`, `trackingUrl`

## API Usage Examples

### Create Template

```bash
curl -X POST http://localhost:3004/api/templates \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "templateId": "welcome-email",
    "name": "Welcome Email",
    "description": "Welcome email for new users",
    "category": "EMAIL",
    "language": "en",
    "content": "<h1>Welcome, {{user.firstName}}!</h1><p>Thank you for joining {{companyName}}.</p>"
  }'
```

### List Templates

```bash
# List all templates
curl http://localhost:3004/api/templates \
  -H "Authorization: Bearer <token>"

# Filter by category
curl "http://localhost:3004/api/templates?category=EMAIL" \
  -H "Authorization: Bearer <token>"

# Paginated with search
curl "http://localhost:3004/api/templates?page=0&size=10&search=welcome" \
  -H "Authorization: Bearer <token>"
```

### Get Template

```bash
curl http://localhost:3004/api/templates/welcome-email \
  -H "Authorization: Bearer <token>"

# Get specific language variant
curl "http://localhost:3004/api/templates/welcome-email?language=es" \
  -H "Authorization: Bearer <token>"
```

### Render Template

```bash
curl -X POST http://localhost:3004/api/templates/welcome-email/render \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "parameters": {
      "user": {
        "firstName": "John",
        "emailVerified": true
      },
      "companyName": "CoreMS",
      "verificationUrl": "https://example.com/verify"
    }
  }'

# Response
{
  "renderedContent": "<h1>Welcome, John!</h1><p>Thank you for joining CoreMS.</p><p>Your email has been verified. You're all set!</p>..."
}
```

### Update Template

```bash
curl -X PUT http://localhost:3004/api/templates/welcome-email \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Welcome Email (Updated)",
    "description": "Updated welcome email template",
    "content": "<h1>Welcome, {{user.firstName}}!</h1><p>Updated content...</p>"
  }'
```

### Delete Template

```bash
curl -X DELETE http://localhost:3004/api/templates/welcome-email \
  -H "Authorization: Bearer <token>"
```

### Get Template Metadata

```bash
curl http://localhost:3004/api/templates/welcome-email/metadata \
  -H "Authorization: Bearer <token>"

# Response
{
  "templateId": "welcome-email",
  "requiredParams": [
    {
      "name": "user.firstName",
      "type": "string"
    },
    {
      "name": "companyName",
      "type": "string"
    },
    {
      "name": "user.emailVerified",
      "type": "boolean"
    },
    {
      "name": "verificationUrl",
      "type": "string"
    }
  ]
}
```

## Handlebars Syntax Reference

### Variables

```handlebars
{{variable}}           <!-- Escaped (safe for HTML) -->
{{{variable}}}         <!-- Unescaped (use with caution) -->
{{object.property}}    <!-- Nested access -->
{{array.[0]}}          <!-- Array index -->
```

### Conditionals

```handlebars
{{#if condition}}
  Content when true
{{else}}
  Content when false
{{/if}}

{{#unless condition}}
  Content when false
{{/unless}}
```

### Iteration

```handlebars
{{#each items}}
  {{this.name}} - {{this.price}}
{{/each}}

{{#each users}}
  {{@index}}: {{this.name}}  <!-- @index is 0-based -->
{{/each}}
```

### Comments

```handlebars
{{! This is a comment, won't appear in output }}
{{!-- Multi-line
      comment --}}
```

## Environment Variables

Copy `.env-example` to `.env` and configure:

```bash
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/template_ms
DATABASE_USER=postgres
DATABASE_PASSWORD=postgres

# JWT Configuration
JWT_PRIVATE_KEY=<PEM-encoded-private-key>
JWT_PUBLIC_KEY=<PEM-encoded-public-key>

# Service Port
TEMPLATE-SERVICE-PORT=3004

# Optional: JWT symmetric key (alternative to RSA)
# AUTH_TOKEN_SECRET=<base64-encoded-secret>
```

## Database Schema

**Schema**: `template_ms`

| Table | Description |
|-------|-------------|
| `templates` | Template definitions with content and metadata |

### Templates Table

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGSERIAL | Primary key |
| `template_id` | VARCHAR(255) | Unique template identifier |
| `language` | VARCHAR(10) | Language code (default: 'en') |
| `name` | VARCHAR(255) | Display name |
| `description` | TEXT | Template description |
| `category` | VARCHAR(50) | EMAIL, SMS, DOCUMENT, REPORT, NOTIFICATION |
| `content` | TEXT | Handlebars template content |
| `required_params` | JSONB | Array of required parameter names |
| `is_deleted` | BOOLEAN | Soft delete flag |
| `created_at` | TIMESTAMP | Creation timestamp |
| `created_by` | VARCHAR(255) | Creator user ID |
| `updated_at` | TIMESTAMP | Last update timestamp |
| `updated_by` | VARCHAR(255) | Last updater user ID |

**Unique Constraint**: `(template_id, language)` - Each template can have one variant per language

## Template Categories

| Category | Use Case | Example |
|----------|----------|---------|
| `EMAIL` | Email templates | Welcome emails, password resets |
| `SMS` | SMS notifications | OTP codes, order updates |
| `DOCUMENT` | Document generation | Invoices, contracts, reports |
| `REPORT` | Report templates | Analytics, summaries |
| `NOTIFICATION` | In-app notifications | Alerts, reminders |

## Roles

From `CoreMsRoles` enum:
- `TEMPLATE_MS_ADMIN` - Full template management access (create, update, delete)
- Standard users can read and render templates

## Architecture

```
template-ms/
├── template-api/          # OpenAPI spec + generated models
├── template-client/       # API client for other services
├── template-service/      # Main application
│   ├── controller/        # REST endpoints
│   ├── service/           # Business logic & rendering
│   ├── repository/        # Data access
│   ├── entity/            # JPA entities
│   ├── exception/         # Custom exceptions
│   └── config/            # Configuration
└── migrations/            # Database migrations
    ├── setup/             # Schema (V1.0.x)
    └── mockdata/          # Seed data (R__xx)
```

## Integration with Other Services

### Communication Service

Use template-ms to render email/SMS content before sending:

```java
// 1. Render template
RenderTemplateRequest renderRequest = new RenderTemplateRequest()
    .parameters(Map.of(
        "user", Map.of("firstName", "John"),
        "companyName", "CoreMS"
    ));

RenderTemplateResponse rendered = templatesApi.renderTemplate(
    "welcome-email", 
    "en", 
    renderRequest
);

// 2. Send via communication-ms
SendEmailRequest emailRequest = new SendEmailRequest()
    .to("user@example.com")
    .subject("Welcome to CoreMS")
    .htmlBody(rendered.getRenderedContent());

communicationApi.sendEmail(emailRequest);
```

### Document Service

Generate documents from templates:

```java
// 1. Render template
RenderTemplateResponse rendered = templatesApi.renderTemplate(
    "invoice-template",
    "en",
    renderRequest
);

// 2. Convert to PDF via document-ms
CreateDocumentRequest docRequest = new CreateDocumentRequest()
    .name("invoice-" + invoiceId + ".pdf")
    .contentType("application/pdf")
    .htmlContent(rendered.getRenderedContent());

documentApi.createDocument(docRequest);
```

## Development

### Build
```bash
# Build all modules
./setup.sh build template-ms

# Build specific module
cd repos/template-ms/template-api
mvn clean install -DskipTests=true
```

### Run Tests
```bash
cd repos/template-ms/template-service
mvn test
```

### Database Migrations
```bash
# Run migrations
./setup.sh migrate template-ms

# Run with seed data
./setup.sh migrate template-ms --mockdata
```

### Logs
```bash
# View logs
./setup.sh logs template-ms

# Follow logs
./setup.sh logs template-ms -f
```

## Performance Considerations

### Template Caching

Compiled templates are cached in memory using `ConcurrentHashMap`:
- First render compiles and caches the template
- Subsequent renders use cached compiled template
- Cache is invalidated on template update/delete
- No external cache (Redis) required for MVP

### Best Practices

1. **Keep templates simple** - Complex logic should be in application code
2. **Pre-process data** - Format dates, numbers before passing to template
3. **Minimize nested loops** - Can impact rendering performance
4. **Use partials** - For reusable template sections (future enhancement)
5. **Test templates** - Validate with sample data before production use

## Security Considerations

### XSS Protection

- **Default escaping**: `{{variable}}` automatically escapes HTML
- **Unescaped output**: `{{{variable}}}` renders raw HTML - use only for trusted content
- **Validate input**: Always validate parameters before rendering

### Access Control

- **Admin-only management**: Only `TEMPLATE_MS_ADMIN` can create/update/delete
- **Read access**: All authenticated users can read and render templates
- **Audit trail**: Track who created/updated templates

### Input Validation

- Template content validated for Handlebars syntax
- Required parameters extracted and validated
- Missing parameters rejected with clear error messages

## Troubleshooting

### Template Compilation Fails
- Check Handlebars syntax (balanced {{#if}}/{{/if}}, etc.)
- Verify all block helpers are closed
- Use template metadata endpoint to see required parameters

### Rendering Fails
- Ensure all required parameters are provided
- Check parameter types match expected values
- Review error message for missing/invalid parameters

### Template Not Found
- Verify templateId is correct (case-sensitive)
- Check language parameter (defaults to 'en')
- Ensure template is not soft-deleted

## Links

- **Main Project**: https://github.com/CoreWebMicroservices/corems-project
- **OpenAPI Spec**: `template-api/src/main/resources/template-ms-api.yaml`
- **Handlebars Documentation**: https://handlebarsjs.com/

## License

See main project repository for license information.
