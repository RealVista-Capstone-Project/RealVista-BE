-- Remove the hero image row that left a large empty band when images were blocked
-- or failed to load. Single compact gradient header flows straight into the body.

-- =====================================================================
-- PRICE_DROP_EMAIL (vi)
-- =====================================================================
UPDATE notification_templates
SET content_body = $BODY$
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width,initial-scale=1"/>
  <title>RealVista · Cập nhật giá</title>
</head>
<body style="margin:0;padding:0;background-color:#eff6ff;font-family:'Segoe UI',Roboto,Helvetica,Arial,sans-serif;color:#0f172a;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background-color:#eff6ff;padding:32px 12px;">
  <tr>
    <td align="center">
      <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="max-width:600px;background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 10px 40px rgba(2,132,199,0.15);">
        <tr>
          <td align="center" style="background:#0284C7;background-image:linear-gradient(135deg,#0EA5E9 0%,#0284C7 50%,#0369A1 100%);padding:32px 24px 28px;">
            <div style="font-size:36px;line-height:1;">&#127968;</div>
            <p style="margin:14px 0 6px;font-size:24px;font-weight:800;color:#ffffff;letter-spacing:0.04em;">RealVista</p>
            <p style="margin:0;font-size:13px;color:rgba(255,255,255,0.92);text-transform:uppercase;letter-spacing:0.14em;">Cập nhật giá tin đăng</p>
          </td>
        </tr>
        <tr>
          <td style="padding:32px 28px 8px;">
            <p style="margin:0 0 8px;font-size:13px;color:#0284C7;text-transform:uppercase;letter-spacing:0.08em;font-weight:600;">Tin vui · Giảm giá</p>
            <h1 style="margin:0 0 16px;font-size:22px;line-height:1.3;color:#0f172a;font-weight:700;">{{listingName}}</h1>
            <p style="margin:0 0 24px;font-size:15px;color:#475569;line-height:1.6;">
              Bất động sản bạn đã lưu vừa <strong style="color:#0EA5E9;">giảm giá</strong>. Đừng bỏ lỡ cơ hội này!
            </p>
            <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="border:1px solid #e0f2fe;border-radius:12px;border-collapse:separate;overflow:hidden;margin-bottom:24px;background:#f0f9ff;">
              <tr><td style="padding:14px 18px;font-size:11px;color:#0369a1;text-transform:uppercase;letter-spacing:0.08em;font-weight:600;">Giá trước đây</td></tr>
              <tr><td style="padding:0 18px 14px;font-size:18px;color:#94a3b8;text-decoration:line-through;">{{oldPrice}}</td></tr>
              <tr><td style="padding:14px 18px 6px;border-top:1px dashed #bae6fd;font-size:11px;color:#0369a1;text-transform:uppercase;letter-spacing:0.08em;font-weight:600;">Giá mới</td></tr>
              <tr><td style="padding:0 18px 16px;font-size:28px;font-weight:800;color:#0284C7;">{{newPrice}}</td></tr>
              <tr><td style="padding:14px 18px;background:#0EA5E9;color:#ffffff;font-size:14px;font-weight:600;">
                Tiết kiệm: {{diff}} &nbsp;·&nbsp; Giảm {{percent}}%
              </td></tr>
            </table>
            <table role="presentation" cellspacing="0" cellpadding="0" style="margin:8px 0 4px;">
              <tr>
                <td style="border-radius:10px;background:#0284C7;box-shadow:0 4px 12px rgba(2,132,199,0.35);">
                  <a href="{{listingUrl}}"
                     style="display:inline-block;padding:14px 32px;font-size:15px;font-weight:700;color:#ffffff;text-decoration:none;letter-spacing:0.02em;">
                    Xem tin đăng &rarr;
                  </a>
                </td>
              </tr>
            </table>
            <p style="margin:20px 0 0;font-size:12px;color:#94a3b8;line-height:1.5;">
              Nếu nút không hoạt động, sao chép liên kết: <a href="{{listingUrl}}" style="color:#0284C7;text-decoration:none;">{{listingUrl}}</a>
            </p>
          </td>
        </tr>
        <tr>
          <td style="padding:20px 28px 28px;border-top:1px solid #e0f2fe;background:#f8fafc;">
            <p style="margin:0;font-size:11px;color:#94a3b8;text-align:center;line-height:1.6;">
              © RealVista · Thông báo tự động, vui lòng không trả lời email này.<br/>
              Bạn nhận được email này vì đã lưu tin đăng trong tài khoản RealVista.
            </p>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>
</body>
</html>
$BODY$,
    updated_at = NOW()
WHERE template_key = 'PRICE_DROP_EMAIL' AND language = 'vi';

-- =====================================================================
-- PRICE_DROP_EMAIL (en)
-- =====================================================================
UPDATE notification_templates
SET content_body = $BODY$
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width,initial-scale=1"/>
  <title>RealVista · Price update</title>
</head>
<body style="margin:0;padding:0;background-color:#eff6ff;font-family:'Segoe UI',Roboto,Helvetica,Arial,sans-serif;color:#0f172a;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background-color:#eff6ff;padding:32px 12px;">
  <tr>
    <td align="center">
      <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="max-width:600px;background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 10px 40px rgba(2,132,199,0.15);">
        <tr>
          <td align="center" style="background:#0284C7;background-image:linear-gradient(135deg,#0EA5E9 0%,#0284C7 50%,#0369A1 100%);padding:32px 24px 28px;">
            <div style="font-size:36px;line-height:1;">&#127968;</div>
            <p style="margin:14px 0 6px;font-size:24px;font-weight:800;color:#ffffff;letter-spacing:0.04em;">RealVista</p>
            <p style="margin:0;font-size:13px;color:rgba(255,255,255,0.92);text-transform:uppercase;letter-spacing:0.14em;">Saved listing update</p>
          </td>
        </tr>
        <tr>
          <td style="padding:32px 28px 8px;">
            <p style="margin:0 0 8px;font-size:13px;color:#0284C7;text-transform:uppercase;letter-spacing:0.08em;font-weight:600;">Good news · Price drop</p>
            <h1 style="margin:0 0 16px;font-size:22px;line-height:1.3;color:#0f172a;font-weight:700;">{{listingName}}</h1>
            <p style="margin:0 0 24px;font-size:15px;color:#475569;line-height:1.6;">
              A property you bookmarked just had a <strong style="color:#0EA5E9;">price drop</strong>. Don't miss this opportunity!
            </p>
            <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="border:1px solid #e0f2fe;border-radius:12px;border-collapse:separate;overflow:hidden;margin-bottom:24px;background:#f0f9ff;">
              <tr><td style="padding:14px 18px;font-size:11px;color:#0369a1;text-transform:uppercase;letter-spacing:0.08em;font-weight:600;">Previous price</td></tr>
              <tr><td style="padding:0 18px 14px;font-size:18px;color:#94a3b8;text-decoration:line-through;">{{oldPrice}}</td></tr>
              <tr><td style="padding:14px 18px 6px;border-top:1px dashed #bae6fd;font-size:11px;color:#0369a1;text-transform:uppercase;letter-spacing:0.08em;font-weight:600;">New price</td></tr>
              <tr><td style="padding:0 18px 16px;font-size:28px;font-weight:800;color:#0284C7;">{{newPrice}}</td></tr>
              <tr><td style="padding:14px 18px;background:#0EA5E9;color:#ffffff;font-size:14px;font-weight:600;">You save: {{diff}} &nbsp;·&nbsp; {{percent}}% off</td></tr>
            </table>
            <table role="presentation" cellspacing="0" cellpadding="0" style="margin:8px 0 4px;">
              <tr><td style="border-radius:10px;background:#0284C7;box-shadow:0 4px 12px rgba(2,132,199,0.35);">
                <a href="{{listingUrl}}" style="display:inline-block;padding:14px 32px;font-size:15px;font-weight:700;color:#ffffff;text-decoration:none;letter-spacing:0.02em;">View listing &rarr;</a>
              </td></tr>
            </table>
            <p style="margin:20px 0 0;font-size:12px;color:#94a3b8;line-height:1.5;">
              If the button doesn't work, copy this link: <a href="{{listingUrl}}" style="color:#0284C7;text-decoration:none;">{{listingUrl}}</a>
            </p>
          </td>
        </tr>
        <tr>
          <td style="padding:20px 28px 28px;border-top:1px solid #e0f2fe;background:#f8fafc;">
            <p style="margin:0;font-size:11px;color:#94a3b8;text-align:center;line-height:1.6;">
              © RealVista · Automated notification — please do not reply.<br/>
              You're receiving this email because you saved this listing in your RealVista account.
            </p>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>
</body>
</html>
$BODY$,
    updated_at = NOW()
WHERE template_key = 'PRICE_DROP_EMAIL' AND language = 'en';

-- =====================================================================
-- PRICE_INCREASE_EMAIL (vi)
-- =====================================================================
UPDATE notification_templates
SET content_body = $BODY$
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width,initial-scale=1"/>
  <title>RealVista · Cập nhật giá</title>
</head>
<body style="margin:0;padding:0;background-color:#eff6ff;font-family:'Segoe UI',Roboto,Helvetica,Arial,sans-serif;color:#0f172a;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background-color:#eff6ff;padding:32px 12px;">
  <tr>
    <td align="center">
      <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="max-width:600px;background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 10px 40px rgba(2,132,199,0.15);">
        <tr>
          <td align="center" style="background:#0284C7;background-image:linear-gradient(135deg,#0EA5E9 0%,#0284C7 50%,#0369A1 100%);padding:32px 24px 28px;">
            <div style="font-size:36px;line-height:1;">&#127981;</div>
            <p style="margin:14px 0 6px;font-size:24px;font-weight:800;color:#ffffff;letter-spacing:0.04em;">RealVista</p>
            <p style="margin:0;font-size:13px;color:rgba(255,255,255,0.92);text-transform:uppercase;letter-spacing:0.14em;">Cập nhật giá tin đăng</p>
          </td>
        </tr>
        <tr>
          <td style="padding:32px 28px 8px;">
            <p style="margin:0 0 8px;font-size:13px;color:#0284C7;text-transform:uppercase;letter-spacing:0.08em;font-weight:600;">Cập nhật · Giá mới</p>
            <h1 style="margin:0 0 16px;font-size:22px;line-height:1.3;color:#0f172a;font-weight:700;">{{listingName}}</h1>
            <p style="margin:0 0 24px;font-size:15px;color:#475569;line-height:1.6;">
              Bất động sản bạn đã lưu vừa <strong style="color:#0284C7;">điều chỉnh giá tăng</strong> so với trước. Hãy xem ngay để cân nhắc.
            </p>
            <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="border:1px solid #e0f2fe;border-radius:12px;border-collapse:separate;overflow:hidden;margin-bottom:24px;background:#f0f9ff;">
              <tr><td style="padding:14px 18px;font-size:11px;color:#0369a1;text-transform:uppercase;letter-spacing:0.08em;font-weight:600;">Giá trước đây</td></tr>
              <tr><td style="padding:0 18px 14px;font-size:18px;color:#475569;">{{oldPrice}}</td></tr>
              <tr><td style="padding:14px 18px 6px;border-top:1px dashed #bae6fd;font-size:11px;color:#0369a1;text-transform:uppercase;letter-spacing:0.08em;font-weight:600;">Giá mới</td></tr>
              <tr><td style="padding:0 18px 16px;font-size:28px;font-weight:800;color:#0284C7;">{{newPrice}}</td></tr>
              <tr><td style="padding:14px 18px;background:#0EA5E9;color:#ffffff;font-size:14px;font-weight:600;">Chênh lệch: {{diff}} &nbsp;·&nbsp; +{{percent}}%</td></tr>
            </table>
            <table role="presentation" cellspacing="0" cellpadding="0" style="margin:8px 0 4px;">
              <tr><td style="border-radius:10px;background:#0284C7;box-shadow:0 4px 12px rgba(2,132,199,0.35);">
                <a href="{{listingUrl}}" style="display:inline-block;padding:14px 32px;font-size:15px;font-weight:700;color:#ffffff;text-decoration:none;letter-spacing:0.02em;">Xem tin đăng &rarr;</a>
              </td></tr>
            </table>
            <p style="margin:20px 0 0;font-size:12px;color:#94a3b8;line-height:1.5;">
              Nếu nút không hoạt động, sao chép liên kết: <a href="{{listingUrl}}" style="color:#0284C7;text-decoration:none;">{{listingUrl}}</a>
            </p>
          </td>
        </tr>
        <tr>
          <td style="padding:20px 28px 28px;border-top:1px solid #e0f2fe;background:#f8fafc;">
            <p style="margin:0;font-size:11px;color:#94a3b8;text-align:center;line-height:1.6;">
              © RealVista · Thông báo tự động, vui lòng không trả lời email này.<br/>
              Bạn nhận được email này vì đã lưu tin đăng trong tài khoản RealVista.
            </p>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>
</body>
</html>
$BODY$,
    updated_at = NOW()
WHERE template_key = 'PRICE_INCREASE_EMAIL' AND language = 'vi';

-- =====================================================================
-- PRICE_INCREASE_EMAIL (en)
-- =====================================================================
UPDATE notification_templates
SET content_body = $BODY$
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width,initial-scale=1"/>
  <title>RealVista · Price update</title>
</head>
<body style="margin:0;padding:0;background-color:#eff6ff;font-family:'Segoe UI',Roboto,Helvetica,Arial,sans-serif;color:#0f172a;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background-color:#eff6ff;padding:32px 12px;">
  <tr>
    <td align="center">
      <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="max-width:600px;background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 10px 40px rgba(2,132,199,0.15);">
        <tr>
          <td align="center" style="background:#0284C7;background-image:linear-gradient(135deg,#0EA5E9 0%,#0284C7 50%,#0369A1 100%);padding:32px 24px 28px;">
            <div style="font-size:36px;line-height:1;">&#127981;</div>
            <p style="margin:14px 0 6px;font-size:24px;font-weight:800;color:#ffffff;letter-spacing:0.04em;">RealVista</p>
            <p style="margin:0;font-size:13px;color:rgba(255,255,255,0.92);text-transform:uppercase;letter-spacing:0.14em;">Saved listing update</p>
          </td>
        </tr>
        <tr>
          <td style="padding:32px 28px 8px;">
            <p style="margin:0 0 8px;font-size:13px;color:#0284C7;text-transform:uppercase;letter-spacing:0.08em;font-weight:600;">Update · New price</p>
            <h1 style="margin:0 0 16px;font-size:22px;line-height:1.3;color:#0f172a;font-weight:700;">{{listingName}}</h1>
            <p style="margin:0 0 24px;font-size:15px;color:#475569;line-height:1.6;">
              A property you bookmarked has a <strong style="color:#0284C7;">new higher price</strong>. Take another look while it's still on the market.
            </p>
            <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="border:1px solid #e0f2fe;border-radius:12px;border-collapse:separate;overflow:hidden;margin-bottom:24px;background:#f0f9ff;">
              <tr><td style="padding:14px 18px;font-size:11px;color:#0369a1;text-transform:uppercase;letter-spacing:0.08em;font-weight:600;">Previous price</td></tr>
              <tr><td style="padding:0 18px 14px;font-size:18px;color:#475569;">{{oldPrice}}</td></tr>
              <tr><td style="padding:14px 18px 6px;border-top:1px dashed #bae6fd;font-size:11px;color:#0369a1;text-transform:uppercase;letter-spacing:0.08em;font-weight:600;">New price</td></tr>
              <tr><td style="padding:0 18px 16px;font-size:28px;font-weight:800;color:#0284C7;">{{newPrice}}</td></tr>
              <tr><td style="padding:14px 18px;background:#0EA5E9;color:#ffffff;font-size:14px;font-weight:600;">Change: {{diff}} &nbsp;·&nbsp; +{{percent}}%</td></tr>
            </table>
            <table role="presentation" cellspacing="0" cellpadding="0" style="margin:8px 0 4px;">
              <tr><td style="border-radius:10px;background:#0284C7;box-shadow:0 4px 12px rgba(2,132,199,0.35);">
                <a href="{{listingUrl}}" style="display:inline-block;padding:14px 32px;font-size:15px;font-weight:700;color:#ffffff;text-decoration:none;letter-spacing:0.02em;">View listing &rarr;</a>
              </td></tr>
            </table>
            <p style="margin:20px 0 0;font-size:12px;color:#94a3b8;line-height:1.5;">
              If the button doesn't work, copy this link: <a href="{{listingUrl}}" style="color:#0284C7;text-decoration:none;">{{listingUrl}}</a>
            </p>
          </td>
        </tr>
        <tr>
          <td style="padding:20px 28px 28px;border-top:1px solid #e0f2fe;background:#f8fafc;">
            <p style="margin:0;font-size:11px;color:#94a3b8;text-align:center;line-height:1.6;">
              © RealVista · Automated notification — please do not reply.<br/>
              You're receiving this email because you saved this listing in your RealVista account.
            </p>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>
</body>
</html>
$BODY$,
    updated_at = NOW()
WHERE template_key = 'PRICE_INCREASE_EMAIL' AND language = 'en';
