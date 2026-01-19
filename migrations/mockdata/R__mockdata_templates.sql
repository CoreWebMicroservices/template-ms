-- Sample template for testing and demonstration

-- Welcome email template (English)
INSERT INTO template (template_id, language, name, description, content, category, param_schema)
VALUES (
    'welcome-email',
    'en',
    'Welcome Email',
    'Sent to new users after registration',
    '<html>
<head><title>Welcome</title></head>
<body>
    <h1>Welcome {{userName}}!</h1>
    <p>Thank you for joining our platform.</p>
    <p>Please click the link below to activate your account:</p>
    <p><a href="{{activationLink}}">Activate Account</a></p>
    <p>Best regards,<br>The Team</p>
</body>
</html>',
    'EMAIL',
    '{"userName": {"required": true, "type": "string"}, "activationLink": {"required": true, "type": "string", "pattern": "^https?://.+"}}'::jsonb
)
ON CONFLICT (template_id, language) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    content = EXCLUDED.content,
    category = EXCLUDED.category,
    param_schema = EXCLUDED.param_schema,
    updated_at = CURRENT_TIMESTAMP;

-- Password reset email template (English)
INSERT INTO template (template_id, language, name, description, content, category, param_schema)
VALUES (
    'password-reset-email',
    'en',
    'Password Reset Email',
    'Sent when user requests password reset',
    '<html>
<head><title>Password Reset</title></head>
<body>
    <h1>Password Reset Request</h1>
    <p>Hello {{userName}},</p>
    <p>We received a request to reset your password.</p>
    <p>Click the link below to reset your password:</p>
    <p><a href="{{resetLink}}">Reset Password</a></p>
    <p>This link will expire in {{expirationHours}} hours.</p>
    <p>If you did not request this, please ignore this email.</p>
</body>
</html>',
    'EMAIL',
    '{"userName": {"required": true, "type": "string"}, "resetLink": {"required": true, "type": "string"}, "expirationHours": {"required": false, "type": "number"}}'::jsonb
)
ON CONFLICT (template_id, language) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    content = EXCLUDED.content,
    category = EXCLUDED.category,
    param_schema = EXCLUDED.param_schema,
    updated_at = CURRENT_TIMESTAMP;

-- SMS verification template (English)
INSERT INTO template (template_id, language, name, description, content, category, param_schema)
VALUES (
    'sms-verification',
    'en',
    'SMS Verification Code',
    'SMS template for sending verification codes',
    'Your verification code is: {{code}}. Valid for {{validMinutes}} minutes.',
    'SMS',
    '{"code": {"required": true, "type": "string", "pattern": "^[0-9]{6}$"}, "validMinutes": {"required": false, "type": "number"}}'::jsonb
)
ON CONFLICT (template_id, language) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    content = EXCLUDED.content,
    category = EXCLUDED.category,
    param_schema = EXCLUDED.param_schema,
    updated_at = CURRENT_TIMESTAMP;

-- Invoice document template (English)
INSERT INTO template (template_id, language, name, description, content, category, param_schema)
VALUES (
    'invoice-document',
    'en',
    'Invoice Document',
    'Template for generating invoice PDFs',
    '<html>
<head>
    <title>Invoice</title>
    <style>
        table { width: 100%; border-collapse: collapse; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
        .total { font-weight: bold; }
    </style>
</head>
<body>
    <h1>Invoice #{{invoiceNumber}}</h1>
    <p>Date: {{invoiceDate}}</p>
    <p>Customer: {{customerName}}</p>
    <p>Address: {{customerAddress}}</p>
    
    <h2>Items</h2>
    <table>
        <tr>
            <th>Item</th>
            <th>Quantity</th>
            <th>Price</th>
            <th>Total</th>
        </tr>
        {{#each items}}
        <tr>
            <td>{{name}}</td>
            <td>{{quantity}}</td>
            <td>$${{price}}</td>
            <td>$${{total}}</td>
        </tr>
        {{/each}}
    </table>
    
    <p class="total">Total: $${{totalAmount}}</p>
    
    {{#if notes}}
    <p>Notes: {{notes}}</p>
    {{/if}}
</body>
</html>',
    'DOCUMENT',
    '{"invoiceNumber": {"required": true, "type": "string"}, "invoiceDate": {"required": true, "type": "string"}, "customerName": {"required": true, "type": "string"}, "customerAddress": {"required": true, "type": "string"}, "items": {"required": true, "type": "array"}, "totalAmount": {"required": true, "type": "number"}, "notes": {"required": false, "type": "string"}}'::jsonb
)
ON CONFLICT (template_id, language) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    content = EXCLUDED.content,
    category = EXCLUDED.category,
    param_schema = EXCLUDED.param_schema,
    updated_at = CURRENT_TIMESTAMP;

-- Common header fragment (English)
INSERT INTO template (template_id, language, name, description, content, category, param_schema)
VALUES (
    'email-header',
    'en',
    'Email Header Fragment',
    'Reusable header for email templates',
    '<div style="background-color: #4CAF50; color: white; padding: 20px; text-align: center;">
    <h1>{{companyName}}</h1>
    {{#if tagline}}
    <p>{{tagline}}</p>
    {{/if}}
</div>',
    'COMMON',
    '{"companyName": {"required": true, "type": "string"}, "tagline": {"required": false, "type": "string"}}'::jsonb
)
ON CONFLICT (template_id, language) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    content = EXCLUDED.content,
    category = EXCLUDED.category,
    param_schema = EXCLUDED.param_schema,
    updated_at = CURRENT_TIMESTAMP;
