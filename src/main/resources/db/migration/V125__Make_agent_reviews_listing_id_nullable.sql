-- Allow agent reviews without a listing reference.
ALTER TABLE agent_reviews
    ALTER COLUMN listing_id DROP NOT NULL;
