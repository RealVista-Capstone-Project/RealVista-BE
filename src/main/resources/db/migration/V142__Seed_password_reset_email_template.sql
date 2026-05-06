INSERT INTO notification_templates (template_key, name, type, language, title, content_body) VALUES
(
    'PASSWORD_RESET',
    'Password Reset',
    'EMAIL',
    'en',
    'Reset your RealVista password',
    'Hi {{userName}}, we received a request to reset your RealVista password. Click the link below to choose a new password. This link expires in {{expiryMinutes}} minutes.<br><br><a href="{{resetLink}}">{{resetLink}}</a><br><br>If you did not request this, you can ignore this email.'
),
(
    'PASSWORD_RESET',
    'Đặt lại mật khẩu',
    'EMAIL',
    'vi',
    'Đặt lại mật khẩu RealVista',
    'Chào {{userName}}, chúng tôi nhận được yêu cầu đặt lại mật khẩu RealVista của bạn. Nhấp vào liên kết bên dưới để đặt mật khẩu mới. Liên kết hết hạn sau {{expiryMinutes}} phút.<br><br><a href="{{resetLink}}">{{resetLink}}</a><br><br>Nếu bạn không yêu cầu việc này, vui lòng bỏ qua email này.'
);
