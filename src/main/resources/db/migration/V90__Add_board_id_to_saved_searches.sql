-- V100__Add_board_id_to_saved_searches.sql
ALTER TABLE saved_searches ADD COLUMN board_id VARCHAR(100) DEFAULT 'Mặc định';
