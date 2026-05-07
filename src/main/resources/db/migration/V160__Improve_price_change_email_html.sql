-- Richer HTML layout for bookmark price-change emails (inline styles for clients)
UPDATE notification_templates
SET content_body = $BODY$
<!DOCTYPE html>
<html lang="vi">
<head><meta charset="UTF-8"/><meta name="viewport" content="width=device-width,initial-scale=1"/></head>
<body style="margin:0;padding:0;background-color:#f1f5f9;font-family:Segoe UI,Roboto,Helvetica,Arial,sans-serif;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background-color:#f1f5f9;padding:24px 12px;">
  <tr><td align="center">
    <table role="presentation" width="100%" style="max-width:560px;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 24px rgba(15,23,42,0.08);">
      <tr><td style="background:linear-gradient(135deg,#4f46e5 0%,#7c3aed 100%);padding:20px 24px;">
        <p style="margin:0;font-size:18px;font-weight:700;color:#ffffff;letter-spacing:0.02em;">RealVista</p>
        <p style="margin:8px 0 0;font-size:13px;color:rgba(255,255,255,0.9);">Cập nhật tin đăng bạn đã lưu</p>
      </td></tr>
      <tr><td style="padding:28px 24px 8px;">
        <p style="margin:0 0 16px;font-size:15px;color:#334155;line-height:1.5;">Xin chào,</p>
        <p style="margin:0 0 20px;font-size:15px;color:#334155;line-height:1.6;">
          Bất động sản <strong style="color:#0f172a;">{{listingName}}</strong> mà bạn đã lưu vừa <strong style="color:#059669;">giảm giá</strong>.
        </p>
        <table role="presentation" width="100%" style="border:1px solid #e2e8f0;border-radius:8px;border-collapse:separate;border-spacing:0;overflow:hidden;margin-bottom:24px;">
          <tr><td style="padding:14px 16px;background:#f8fafc;font-size:12px;text-transform:uppercase;letter-spacing:0.06em;color:#64748b;">Giá trước</td></tr>
          <tr><td style="padding:12px 16px 16px;font-size:18px;color:#94a3b8;text-decoration:line-through;">{{oldPrice}}</td></tr>
          <tr><td style="padding:14px 16px;background:#f8fafc;font-size:12px;text-transform:uppercase;letter-spacing:0.06em;color:#64748b;">Giá mới</td></tr>
          <tr><td style="padding:12px 16px 18px;font-size:22px;font-weight:700;color:#4f46e5;">{{newPrice}}</td></tr>
          <tr><td style="padding:12px 16px 16px;background:#ecfdf5;border-top:1px solid #d1fae5;font-size:14px;color:#047857;">
            Tiết kiệm: <strong>{{diff}}</strong> &nbsp;·&nbsp; <strong>{{percent}}%</strong>
          </td></tr>
        </table>
        <table role="presentation" cellspacing="0" cellpadding="0"><tr><td style="border-radius:8px;background:#4f46e5;">
          <a href="{{listingUrl}}" style="display:inline-block;padding:14px 28px;font-size:15px;font-weight:600;color:#ffffff;text-decoration:none;">Xem tin đăng</a>
        </td></tr></table>
        <p style="margin:24px 0 0;font-size:12px;color:#94a3b8;line-height:1.5;">Nếu nút không hoạt động, sao chép liên kết: <a href="{{listingUrl}}" style="color:#4f46e5;">{{listingUrl}}</a></p>
      </td></tr>
      <tr><td style="padding:16px 24px 24px;border-top:1px solid #f1f5f9;">
        <p style="margin:0;font-size:11px;color:#94a3b8;text-align:center;">© RealVista · Thông báo tự động, vui lòng không trả lời email này.</p>
      </td></tr>
    </table>
  </td></tr>
</table>
</body>
</html>
$BODY$,
    updated_at = NOW()
WHERE template_key = 'PRICE_DROP_EMAIL' AND language = 'vi';

UPDATE notification_templates
SET content_body = $BODY$
<!DOCTYPE html>
<html lang="en">
<head><meta charset="UTF-8"/><meta name="viewport" content="width=device-width,initial-scale=1"/></head>
<body style="margin:0;padding:0;background-color:#f1f5f9;font-family:Segoe UI,Roboto,Helvetica,Arial,sans-serif;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background-color:#f1f5f9;padding:24px 12px;">
  <tr><td align="center">
    <table role="presentation" width="100%" style="max-width:560px;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 24px rgba(15,23,42,0.08);">
      <tr><td style="background:linear-gradient(135deg,#4f46e5 0%,#7c3aed 100%);padding:20px 24px;">
        <p style="margin:0;font-size:18px;font-weight:700;color:#ffffff;letter-spacing:0.02em;">RealVista</p>
        <p style="margin:8px 0 0;font-size:13px;color:rgba(255,255,255,0.9);">An update on a listing you saved</p>
      </td></tr>
      <tr><td style="padding:28px 24px 8px;">
        <p style="margin:0 0 16px;font-size:15px;color:#334155;line-height:1.5;">Hi there,</p>
        <p style="margin:0 0 20px;font-size:15px;color:#334155;line-height:1.6;">
          <strong style="color:#0f172a;">{{listingName}}</strong>, which you bookmarked, just had a <strong style="color:#059669;">price drop</strong>.
        </p>
        <table role="presentation" width="100%" style="border:1px solid #e2e8f0;border-radius:8px;border-collapse:separate;border-spacing:0;overflow:hidden;margin-bottom:24px;">
          <tr><td style="padding:14px 16px;background:#f8fafc;font-size:12px;text-transform:uppercase;letter-spacing:0.06em;color:#64748b;">Previous price</td></tr>
          <tr><td style="padding:12px 16px 16px;font-size:18px;color:#94a3b8;text-decoration:line-through;">{{oldPrice}}</td></tr>
          <tr><td style="padding:14px 16px;background:#f8fafc;font-size:12px;text-transform:uppercase;letter-spacing:0.06em;color:#64748b;">New price</td></tr>
          <tr><td style="padding:12px 16px 18px;font-size:22px;font-weight:700;color:#4f46e5;">{{newPrice}}</td></tr>
          <tr><td style="padding:12px 16px 16px;background:#ecfdf5;border-top:1px solid #d1fae5;font-size:14px;color:#047857;">
            You save: <strong>{{diff}}</strong> &nbsp;·&nbsp; <strong>{{percent}}%</strong>
          </td></tr>
        </table>
        <table role="presentation" cellspacing="0" cellpadding="0"><tr><td style="border-radius:8px;background:#4f46e5;">
          <a href="{{listingUrl}}" style="display:inline-block;padding:14px 28px;font-size:15px;font-weight:600;color:#ffffff;text-decoration:none;">View listing</a>
        </td></tr></table>
        <p style="margin:24px 0 0;font-size:12px;color:#94a3b8;line-height:1.5;">If the button does not work, copy this link: <a href="{{listingUrl}}" style="color:#4f46e5;">{{listingUrl}}</a></p>
      </td></tr>
      <tr><td style="padding:16px 24px 24px;border-top:1px solid #f1f5f9;">
        <p style="margin:0;font-size:11px;color:#94a3b8;text-align:center;">© RealVista · Automated message, please do not reply.</p>
      </td></tr>
    </table>
  </td></tr>
</table>
</body>
</html>
$BODY$,
    updated_at = NOW()
WHERE template_key = 'PRICE_DROP_EMAIL' AND language = 'en';

UPDATE notification_templates
SET content_body = $BODY$
<!DOCTYPE html>
<html lang="vi">
<head><meta charset="UTF-8"/><meta name="viewport" content="width=device-width,initial-scale=1"/></head>
<body style="margin:0;padding:0;background-color:#f1f5f9;font-family:Segoe UI,Roboto,Helvetica,Arial,sans-serif;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background-color:#f1f5f9;padding:24px 12px;">
  <tr><td align="center">
    <table role="presentation" width="100%" style="max-width:560px;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 24px rgba(15,23,42,0.08);">
      <tr><td style="background:linear-gradient(135deg,#7c3aed 0%,#db2777 100%);padding:20px 24px;">
        <p style="margin:0;font-size:18px;font-weight:700;color:#ffffff;letter-spacing:0.02em;">RealVista</p>
        <p style="margin:8px 0 0;font-size:13px;color:rgba(255,255,255,0.9);">Cập nhật giá tin đăng đã lưu</p>
      </td></tr>
      <tr><td style="padding:28px 24px 8px;">
        <p style="margin:0 0 16px;font-size:15px;color:#334155;line-height:1.5;">Xin chào,</p>
        <p style="margin:0 0 20px;font-size:15px;color:#334155;line-height:1.6;">
          Bất động sản <strong style="color:#0f172a;">{{listingName}}</strong> mà bạn đã lưu có <strong style="color:#c2410c;">mức giá mới</strong> (tăng so với trước).
        </p>
        <table role="presentation" width="100%" style="border:1px solid #e2e8f0;border-radius:8px;border-collapse:separate;border-spacing:0;overflow:hidden;margin-bottom:24px;">
          <tr><td style="padding:14px 16px;background:#f8fafc;font-size:12px;text-transform:uppercase;letter-spacing:0.06em;color:#64748b;">Giá trước</td></tr>
          <tr><td style="padding:12px 16px 16px;font-size:18px;color:#334155;">{{oldPrice}}</td></tr>
          <tr><td style="padding:14px 16px;background:#f8fafc;font-size:12px;text-transform:uppercase;letter-spacing:0.06em;color:#64748b;">Giá mới</td></tr>
          <tr><td style="padding:12px 16px 18px;font-size:22px;font-weight:700;color:#c2410c;">{{newPrice}}</td></tr>
          <tr><td style="padding:12px 16px 16px;background:#fff7ed;border-top:1px solid #ffedd5;font-size:14px;color:#9a3412;">
            Chênh lệch: <strong>{{diff}}</strong> &nbsp;·&nbsp; <strong>{{percent}}%</strong>
          </td></tr>
        </table>
        <table role="presentation" cellspacing="0" cellpadding="0"><tr><td style="border-radius:8px;background:#7c3aed;">
          <a href="{{listingUrl}}" style="display:inline-block;padding:14px 28px;font-size:15px;font-weight:600;color:#ffffff;text-decoration:none;">Xem tin đăng</a>
        </td></tr></table>
        <p style="margin:24px 0 0;font-size:12px;color:#94a3b8;line-height:1.5;">Liên kết: <a href="{{listingUrl}}" style="color:#7c3aed;">{{listingUrl}}</a></p>
      </td></tr>
      <tr><td style="padding:16px 24px 24px;border-top:1px solid #f1f5f9;">
        <p style="margin:0;font-size:11px;color:#94a3b8;text-align:center;">© RealVista · Thông báo tự động</p>
      </td></tr>
    </table>
  </td></tr>
</table>
</body>
</html>
$BODY$,
    updated_at = NOW()
WHERE template_key = 'PRICE_INCREASE_EMAIL' AND language = 'vi';

UPDATE notification_templates
SET content_body = $BODY$
<!DOCTYPE html>
<html lang="en">
<head><meta charset="UTF-8"/><meta name="viewport" content="width=device-width,initial-scale=1"/></head>
<body style="margin:0;padding:0;background-color:#f1f5f9;font-family:Segoe UI,Roboto,Helvetica,Arial,sans-serif;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background-color:#f1f5f9;padding:24px 12px;">
  <tr><td align="center">
    <table role="presentation" width="100%" style="max-width:560px;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 24px rgba(15,23,42,0.08);">
      <tr><td style="background:linear-gradient(135deg,#7c3aed 0%,#db2777 100%);padding:20px 24px;">
        <p style="margin:0;font-size:18px;font-weight:700;color:#ffffff;letter-spacing:0.02em;">RealVista</p>
        <p style="margin:8px 0 0;font-size:13px;color:rgba(255,255,255,0.9);">Price update on a saved listing</p>
      </td></tr>
      <tr><td style="padding:28px 24px 8px;">
        <p style="margin:0 0 16px;font-size:15px;color:#334155;line-height:1.5;">Hi there,</p>
        <p style="margin:0 0 20px;font-size:15px;color:#334155;line-height:1.6;">
          <strong style="color:#0f172a;">{{listingName}}</strong>, which you bookmarked, has a <strong style="color:#c2410c;">new higher price</strong>.
        </p>
        <table role="presentation" width="100%" style="border:1px solid #e2e8f0;border-radius:8px;border-collapse:separate;border-spacing:0;overflow:hidden;margin-bottom:24px;">
          <tr><td style="padding:14px 16px;background:#f8fafc;font-size:12px;text-transform:uppercase;letter-spacing:0.06em;color:#64748b;">Previous price</td></tr>
          <tr><td style="padding:12px 16px 16px;font-size:18px;color:#334155;">{{oldPrice}}</td></tr>
          <tr><td style="padding:14px 16px;background:#f8fafc;font-size:12px;text-transform:uppercase;letter-spacing:0.06em;color:#64748b;">New price</td></tr>
          <tr><td style="padding:12px 16px 18px;font-size:22px;font-weight:700;color:#c2410c;">{{newPrice}}</td></tr>
          <tr><td style="padding:12px 16px 16px;background:#fff7ed;border-top:1px solid #ffedd5;font-size:14px;color:#9a3412;">
            Change: <strong>{{diff}}</strong> &nbsp;·&nbsp; <strong>{{percent}}%</strong>
          </td></tr>
        </table>
        <table role="presentation" cellspacing="0" cellpadding="0"><tr><td style="border-radius:8px;background:#7c3aed;">
          <a href="{{listingUrl}}" style="display:inline-block;padding:14px 28px;font-size:15px;font-weight:600;color:#ffffff;text-decoration:none;">View listing</a>
        </td></tr></table>
        <p style="margin:24px 0 0;font-size:12px;color:#94a3b8;line-height:1.5;">Link: <a href="{{listingUrl}}" style="color:#7c3aed;">{{listingUrl}}</a></p>
      </td></tr>
      <tr><td style="padding:16px 24px 24px;border-top:1px solid #f1f5f9;">
        <p style="margin:0;font-size:11px;color:#94a3b8;text-align:center;">© RealVista · Automated message</p>
      </td></tr>
    </table>
  </td></tr>
</table>
</body>
</html>
$BODY$,
    updated_at = NOW()
WHERE template_key = 'PRICE_INCREASE_EMAIL' AND language = 'en';
