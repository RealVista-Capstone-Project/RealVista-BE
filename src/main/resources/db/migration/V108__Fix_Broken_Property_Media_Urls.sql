-- V108__Fix_Broken_Property_Media_Urls.sql
-- Fix specifically identified broken Unsplash IDs in property_medias table
-- These malformed IDs caused image loading errors in certain listings (e.g., Logistics)

UPDATE property_medias
SET 
    media_url = REPLACE(media_url, '1558444458-15f1001c1ce9', '1534067783941-51c9c23ea3a3'),
    thumbnail_url = REPLACE(thumbnail_url, '1558444458-15f1001c1ce9', '1534067783941-51c9c23ea3a3')
WHERE media_url LIKE '%1558444458-15f1001c1ce9%';

-- Also handle any potential mismatches in other industrial/logistics categories if they exist
UPDATE property_medias
SET 
    media_url = REPLACE(media_url, '1586528116311-ad861fbc2a4b', '1587293852726-70cdb56c2866'),
    thumbnail_url = REPLACE(thumbnail_url, '1586528116311-ad861fbc2a4b', '1587293852726-70cdb56c2866')
WHERE media_url LIKE '%1586528116311-ad861fbc2a4b%'
  AND media_url NOT LIKE 'https://images.unsplash.com/photo-1587293852726-70cdb56c2866%';
