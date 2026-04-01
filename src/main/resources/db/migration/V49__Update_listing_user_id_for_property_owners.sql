-- V44__Update_listing_user_id_for_property_owners.sql
-- Fix data mismatch: Update the listing's user_id to the property's owner_id
-- if the current listing user_id has the 'OWNER' role but does not own the property.
-- Compatible with PostgreSQL and H2.

UPDATE listings
SET user_id = (
    SELECT owner_id 
    FROM properties 
    WHERE properties.property_id = listings.property_id
)
WHERE listing_id IN (
    SELECT l.listing_id
    FROM listings l
    JOIN properties p ON l.property_id = p.property_id
    WHERE l.user_id != p.owner_id
      AND EXISTS (
          SELECT 1 
          FROM user_roles ur
          JOIN roles r ON ur.role_id = r.role_id
          WHERE ur.user_id = l.user_id 
            AND r.role_code = 'OWNER'
      )
);
