-- e:/FPT_Software_Engineer/Ky9/Capstone/RealVista-BE/src/main/resources/db/migration/V60__Remove_property_id_from_agent_proposals.sql

-- Xóa cột property_id khỏi bảng agent_proposals vì không còn cần thiết gán cứng tại đây
-- Hệ thống sẽ quản lý việc ứng tuyển thông qua bảng engagement_applications
ALTER TABLE agent_proposals DROP COLUMN IF EXISTS property_id;
