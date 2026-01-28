-- CoreMS Email Templates - Simple and Clean

-- ============================================================================
-- STYLE TEMPLATES - Reusable styles
-- ============================================================================

-- CoreMS Email Styles
INSERT INTO template (template_id, language, name, description, content, category, param_schema)
VALUES (
    'corems-styles',
    'en',
    'CoreMS Email Styles',
    'Shared CSS styles for all CoreMS emails',
    '<style>
        body { margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, ''Segoe UI'', Roboto, ''Helvetica Neue'', Arial, sans-serif; background-color: #f5f5f5; }
        .email-container { max-width: 600px; margin: 0 auto; background-color: white; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
        .email-header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px 20px; text-align: center; }
        .email-header h1 { margin: 0; font-size: 28px; font-weight: 600; }
        .email-content { padding: 40px 30px; }
        p { margin: 0 0 20px 0; font-size: 14px; line-height: 1.6; color: #555; }
        a { color: #667eea; text-decoration: none; }
        .btn-primary { display: inline-block; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 12px 30px; text-decoration: none; border-radius: 4px; font-weight: 600; font-size: 14px; }
        .box-info { background-color: #f8f9fa; border-left: 4px solid #667eea; padding: 15px; margin: 30px 0; border-radius: 4px; font-size: 13px; color: #666; }
        .box-warning { background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 30px 0; border-radius: 4px; font-size: 13px; color: #856404; }
        .box-danger { background-color: #f8d7da; border-left: 4px solid #dc3545; padding: 15px; margin: 30px 0; border-radius: 4px; font-size: 13px; color: #721c24; }
        .link-text { margin: 20px 0; font-size: 13px; color: #999; word-break: break-all; }
    </style>',
    'COMMON',
    '{}'::jsonb
)
ON CONFLICT (template_id, language) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    content = EXCLUDED.content,
    category = EXCLUDED.category,
    param_schema = EXCLUDED.param_schema,
    updated_at = CURRENT_TIMESTAMP;

-- ============================================================================
-- EMAIL TEMPLATES
-- ============================================================================

-- Welcome email template
INSERT INTO template (template_id, language, name, description, content, category, param_schema)
VALUES (
    'welcome-email',
    'en',
    'Welcome Email',
    'Sent to new users after registration',
    '<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Welcome to CoreMS</title>
    <style>
        body { margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, ''Segoe UI'', Roboto, ''Helvetica Neue'', Arial, sans-serif; background-color: #f5f5f5; }
        .email-container { max-width: 600px; margin: 0 auto; background-color: white; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
        .email-header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px 20px; text-align: center; }
        .email-header h1 { margin: 0; font-size: 28px; font-weight: 600; }
        .email-content { padding: 40px 30px; }
        p { margin: 0 0 20px 0; font-size: 14px; line-height: 1.6; color: #555; }
        a { color: #667eea; text-decoration: none; }
        .btn-primary { display: inline-block; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 12px 30px; text-decoration: none; border-radius: 4px; font-weight: 600; font-size: 14px; }
        .box-info { background-color: #f8f9fa; border-left: 4px solid #667eea; padding: 15px; margin: 30px 0; border-radius: 4px; font-size: 13px; color: #666; }
    </style>
</head>
<body>
    <div class="email-container">
        <div class="email-header">
            <h1>Welcome to CoreMS</h1>
        </div>
        <div class="email-content">
            <p style="margin: 0 0 20px 0; font-size: 16px; color: #333;">Hi {{firstName}},</p>
            <p>Welcome to CoreMS! We''re thrilled to have you join our platform. Your account is all set and ready to go.</p>
            <div class="box-info">
                <strong>Getting Started:</strong><br>
                Check out our docs and start exploring what CoreMS can do for you.
            </div>
            <div style="text-align: center; margin: 30px 0;">
                <a href="{{appUrl}}" class="btn-primary" style="color: white;">Go to CoreMS</a>
            </div>
            <p>Questions? Just reach out to us anytime.</p>
            <p style="margin: 30px 0 0 0; font-size: 13px; color: #999;">
                Cheers,<br>
                <strong>The CoreMS Team</strong>
            </p>
        </div>
    </div>
</body>
</html>',
    'EMAIL',
    '{"firstName": {"required": true, "type": "string"}, "appUrl": {"required": true, "type": "string"}}'::jsonb
)
ON CONFLICT (template_id, language) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    content = EXCLUDED.content,
    category = EXCLUDED.category,
    param_schema = EXCLUDED.param_schema,
    updated_at = CURRENT_TIMESTAMP;

-- Email verification template
INSERT INTO template (template_id, language, name, description, content, category, param_schema)
VALUES (
    'email-verification',
    'en',
    'Email Verification',
    'Sent when user needs to verify their email address',
    '<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Verify Your Email</title>
    <style>
        body { margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, ''Segoe UI'', Roboto, ''Helvetica Neue'', Arial, sans-serif; background-color: #f5f5f5; }
        .email-container { max-width: 600px; margin: 0 auto; background-color: white; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
        .email-header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px 20px; text-align: center; }
        .email-header h1 { margin: 0; font-size: 28px; font-weight: 600; }
        .email-content { padding: 40px 30px; }
        p { margin: 0 0 20px 0; font-size: 14px; line-height: 1.6; color: #555; }
        a { color: #667eea; text-decoration: none; }
        .btn-primary { display: inline-block; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 12px 30px; text-decoration: none; border-radius: 4px; font-weight: 600; font-size: 14px; }
        .box-warning { background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 30px 0; border-radius: 4px; font-size: 13px; color: #856404; }
        .link-text { margin: 20px 0; font-size: 13px; color: #999; word-break: break-all; }
    </style>
</head>
<body>
    <div class="email-container">
        <div class="email-header">
            <h1>Verify Your Email</h1>
        </div>
        <div class="email-content">
            <p style="margin: 0 0 20px 0; font-size: 16px; color: #333;">Hi {{firstName}},</p>
            <p>Just one more step! Click the button below to verify your email and activate your account.</p>
            <div style="text-align: center; margin: 30px 0;">
                <a href="{{verificationUrl}}" class="btn-primary">Verify Email</a>
            </div>
            <div class="link-text">
                Or copy this link:<br>
                <a href="{{verificationUrl}}">{{verificationUrl}}</a>
            </div>
            <div class="box-warning">
                <strong>Note:</strong> This link expires in 24 hours. If you didn''t sign up, just ignore this.
            </div>
            <p style="margin: 30px 0 0 0; font-size: 13px; color: #999;">
                Cheers,<br>
                <strong>The CoreMS Team</strong>
            </p>
        </div>
    </div>
</body>
</html>',
    'EMAIL',
    '{"firstName": {"required": true, "type": "string"}, "verificationUrl": {"required": true, "type": "string"}}'::jsonb
)
ON CONFLICT (template_id, language) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    content = EXCLUDED.content,
    category = EXCLUDED.category,
    param_schema = EXCLUDED.param_schema,
    updated_at = CURRENT_TIMESTAMP;

-- Password reset email template
INSERT INTO template (template_id, language, name, description, content, category, param_schema)
VALUES (
    'password-reset-email',
    'en',
    'Password Reset',
    'Sent when user requests to reset their password',
    '<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reset Your Password</title>
    <style>
        body { margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, ''Segoe UI'', Roboto, ''Helvetica Neue'', Arial, sans-serif; background-color: #f5f5f5; }
        .email-container { max-width: 600px; margin: 0 auto; background-color: white; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
        .email-header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px 20px; text-align: center; }
        .email-header h1 { margin: 0; font-size: 28px; font-weight: 600; }
        .email-content { padding: 40px 30px; }
        p { margin: 0 0 20px 0; font-size: 14px; line-height: 1.6; color: #555; }
        a { color: #667eea; text-decoration: none; }
        .btn-primary { display: inline-block; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 12px 30px; text-decoration: none; border-radius: 4px; font-weight: 600; font-size: 14px; }
        .box-danger { background-color: #f8d7da; border-left: 4px solid #dc3545; padding: 15px; margin: 30px 0; border-radius: 4px; font-size: 13px; color: #721c24; }
        .link-text { margin: 20px 0; font-size: 13px; color: #999; word-break: break-all; }
    </style>
</head>
<body>
    <div class="email-container">
        <div class="email-header">
            <h1>Reset Your Password</h1>
        </div>
        <div class="email-content">
            <p style="margin: 0 0 20px 0; font-size: 16px; color: #333;">Hi {{firstName}},</p>
            <p>We got a request to reset your password. Click the button below to set a new one.</p>
            <div style="text-align: center; margin: 30px 0;">
                <a href="{{resetUrl}}" class="btn-primary">Reset Password</a>
            </div>
            <div class="link-text">
                Or copy this link:<br>
                <a href="{{resetUrl}}">{{resetUrl}}</a>
            </div>
            <div class="box-danger">
                <strong>Security:</strong> This link expires in 24 hours. If you didn''t request this, just ignore it and your password stays the same.
            </div>
            <p style="margin: 30px 0 0 0; font-size: 13px; color: #999;">
                Cheers,<br>
                <strong>The CoreMS Team</strong>
            </p>
        </div>
    </div>
</body>
</html>',
    'EMAIL',
    '{"firstName": {"required": true, "type": "string"}, "resetUrl": {"required": true, "type": "string"}}'::jsonb
)
ON CONFLICT (template_id, language) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    content = EXCLUDED.content,
    category = EXCLUDED.category,
    param_schema = EXCLUDED.param_schema,
    updated_at = CURRENT_TIMESTAMP;

-- ============================================================================
-- SMS TEMPLATES
-- ============================================================================

-- SMS welcome template
INSERT INTO template (template_id, language, name, description, content, category, param_schema)
VALUES (
    'sms-welcome',
    'en',
    'SMS Welcome',
    'SMS sent to new users after registration',
    'Welcome to CoreMS, {{firstName}}! Your account is ready. Visit {{appUrl}} to get started.',
    'SMS',
    '{"firstName": {"required": true, "type": "string"}, "appUrl": {"required": true, "type": "string"}}'::jsonb
)
ON CONFLICT (template_id, language) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    content = EXCLUDED.content,
    category = EXCLUDED.category,
    param_schema = EXCLUDED.param_schema,
    updated_at = CURRENT_TIMESTAMP;

-- SMS verification code template
INSERT INTO template (template_id, language, name, description, content, category, param_schema)
VALUES (
    'sms-verification',
    'en',
    'SMS Verification Code',
    'SMS template for sending verification codes',
    'Your CoreMS verification code is: {{code}}. Valid for {{validMinutes}} minutes. Do not share this code.',
    'SMS',
    '{"code": {"required": true, "type": "string", "pattern": "^[0-9]{6}$"}, "validMinutes": {"required": false, "type": "number", "default": 10}}'::jsonb
)
ON CONFLICT (template_id, language) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    content = EXCLUDED.content,
    category = EXCLUDED.category,
    param_schema = EXCLUDED.param_schema,
    updated_at = CURRENT_TIMESTAMP;

-- ============================================================================
-- DOCUMENT TEMPLATES
-- ============================================================================

-- Invoice document template
INSERT INTO template (template_id, language, name, description, content, category, param_schema)
VALUES (
    'invoice-document',
    'en',
    'Invoice Document',
    'Template for generating invoice PDFs',
    '<html>
<head>
    <meta charset="UTF-8">
    <title>Invoice</title>
    <style>
        body { font-family: -apple-system, BlinkMacSystemFont, ''Segoe UI'', Roboto, ''Helvetica Neue'', Arial, sans-serif; color: #333; }
        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; }
        .header h1 { margin: 0; font-size: 24px; }
        .content { padding: 30px; }
        .invoice-info { display: flex; justify-content: space-between; margin-bottom: 30px; }
        .info-block { flex: 1; }
        .info-block h3 { margin: 0 0 10px 0; font-size: 12px; color: #999; text-transform: uppercase; }
        .info-block p { margin: 0 0 5px 0; font-size: 14px; }
        table { width: 100%; border-collapse: collapse; margin: 30px 0; }
        th { background-color: #f8f9fa; border-bottom: 2px solid #667eea; padding: 12px; text-align: left; font-weight: 600; font-size: 13px; }
        td { border-bottom: 1px solid #e9ecef; padding: 12px; font-size: 14px; }
        .total-row { background-color: #f8f9fa; font-weight: 600; }
    </style>
</head>
<body>
    <div class="header">
        <h1>Invoice #{{invoiceNumber}}</h1>
    </div>
    <div class="content">
        <div class="invoice-info">
            <div class="info-block">
                <h3>Invoice Date</h3>
                <p>{{invoiceDate}}</p>
            </div>
            <div class="info-block">
                <h3>Due Date</h3>
                <p>{{dueDate}}</p>
            </div>
            <div class="info-block">
                <h3>Invoice ID</h3>
                <p>{{invoiceNumber}}</p>
            </div>
        </div>
        <div class="invoice-info">
            <div class="info-block">
                <h3>Bill To</h3>
                <p><strong>{{customerName}}</strong></p>
                <p>{{customerEmail}}</p>
                <p>{{customerAddress}}</p>
            </div>
            <div class="info-block">
                <h3>From</h3>
                <p><strong>CoreMS</strong></p>
            </div>
        </div>
        <table>
            <thead>
                <tr>
                    <th>Description</th>
                    <th style="text-align: right;">Quantity</th>
                    <th style="text-align: right;">Unit Price</th>
                    <th style="text-align: right;">Total</th>
                </tr>
            </thead>
            <tbody>
                {{#each items}}
                <tr>
                    <td>{{this.description}}</td>
                    <td style="text-align: right;">{{this.quantity}}</td>
                    <td style="text-align: right;">USD {{this.unitPrice}}</td>
                    <td style="text-align: right;">USD {{this.total}}</td>
                </tr>
                {{/each}}
                <tr class="total-row">
                    <td colspan="3" style="text-align: right;">Total Amount:</td>
                    <td style="text-align: right;">USD {{totalAmount}}</td>
                </tr>
            </tbody>
        </table>
        {{#if notes}}
        <div style="background-color: #f8f9fa; padding: 15px; border-radius: 4px; margin: 20px 0;">
            <h3 style="margin: 0 0 10px 0; font-size: 13px;">Notes</h3>
            <p style="margin: 0; font-size: 13px; color: #666;">{{notes}}</p>
        </div>
        {{/if}}
    </div>
</body>
</html>',
    'DOCUMENT',
    '{"invoiceNumber": {"required": true, "type": "string"}, "invoiceDate": {"required": true, "type": "string"}, "dueDate": {"required": true, "type": "string"}, "customerName": {"required": true, "type": "string"}, "customerEmail": {"required": true, "type": "string"}, "customerAddress": {"required": true, "type": "string"}, "items": {"required": true, "type": "array"}, "totalAmount": {"required": true, "type": "number"}, "notes": {"required": false, "type": "string"}}'::jsonb
)
ON CONFLICT (template_id, language) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    content = EXCLUDED.content,
    category = EXCLUDED.category,
    param_schema = EXCLUDED.param_schema,
    updated_at = CURRENT_TIMESTAMP;
