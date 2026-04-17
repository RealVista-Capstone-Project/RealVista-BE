-- V4__Update_ai_feature_constraint.sql
-- Updates chk_ai_feature constraint to include AI_ASSISTANT

-- Drop old constraint
ALTER TABLE ai_feature_usages DROP CONSTRAINT chk_ai_feature;

-- Add updated constraint
ALTER TABLE ai_feature_usages ADD CONSTRAINT chk_ai_feature 
    CHECK (ai_feature IN ('AI_RECOMMEND_LISTING', 'AI_WORTH_BUYING', 'AI_COMPARE', 'AI_PRICE_INSIGHT', 'AI_ASSISTANT'));
