-- V109__Fix_Broken_Unsplash_Media_Urls.sql
-- Fix identified broken Unsplash IDs in property_medias table
-- These IDs were previously returning 404 errors, causing broken images in listings.

UPDATE property_medias
SET 
    media_url = REPLACE(media_url, '1522701705276-c87f4d45d84d', '1484154218962-a197022b5858'),
    thumbnail_url = REPLACE(thumbnail_url, '1522701705276-c87f4d45d84d', '1484154218962-a197022b5858')
WHERE media_url LIKE '%1522701705276-c87f4d45d84d%';

UPDATE property_medias
SET 
    media_url = REPLACE(media_url, '1473580044176-a1859663738e', '1512917774080-9991f1c4c750'),
    thumbnail_url = REPLACE(thumbnail_url, '1473580044176-a1859663738e', '1512917774080-9991f1c4c750')
WHERE media_url LIKE '%1473580044176-a1859663738e%';

UPDATE property_medias
SET 
    media_url = REPLACE(media_url, '1493663284031-b7430acbb116', '1523217582562-09d0def993a6'),
    thumbnail_url = REPLACE(thumbnail_url, '1493663284031-b7430acbb116', '1523217582562-09d0def993a6')
WHERE media_url LIKE '%1493663284031-b7430acbb116%';

UPDATE property_medias
SET 
    media_url = REPLACE(media_url, '1513828583688-c52646db421f', '1564013799919-ab600027ffc6'),
    thumbnail_url = REPLACE(thumbnail_url, '1513828583688-c52646db421f', '1564013799919-ab600027ffc6')
WHERE media_url LIKE '%1513828583688-c52646db421f%';

UPDATE property_medias
SET 
    media_url = REPLACE(media_url, '1497366750744-24f4b052aa66', '1497366811353-6870744d04b2'),
    thumbnail_url = REPLACE(thumbnail_url, '1497366750744-24f4b052aa66', '1497366811353-6870744d04b2')
WHERE media_url LIKE '%1497366750744-24f4b052aa66%';

UPDATE property_medias
SET 
    media_url = REPLACE(media_url, '1504386106331-c9fe10cf468b', '1587293852726-70cdb56c2866'),
    thumbnail_url = REPLACE(thumbnail_url, '1504386106331-c9fe10cf468b', '1587293852726-70cdb56c2866')
WHERE media_url LIKE '%1504386106331-c9fe10cf468b%';

UPDATE property_medias
SET 
    media_url = REPLACE(media_url, '1504917595217-d4dc5f64d0b9', '1587293852726-70cdb56c2866'),
    thumbnail_url = REPLACE(thumbnail_url, '1504917595217-d4dc5f64d0b9', '1587293852726-70cdb56c2866')
WHERE media_url LIKE '%1504917595217-d4dc5f64d0b9%';

UPDATE property_medias
SET 
    media_url = REPLACE(media_url, '1531973572844-3068e1ab62d6', '1501785888041-af3ef285b470'),
    thumbnail_url = REPLACE(thumbnail_url, '1531973572844-3068e1ab62d6', '1501785888041-af3ef285b470')
WHERE media_url LIKE '%1531973572844-3068e1ab62d6%';

UPDATE property_medias
SET 
    media_url = REPLACE(media_url, '1534067783941-51c9c23ea3a3', '1587293852726-70cdb56c2866'),
    thumbnail_url = REPLACE(thumbnail_url, '1534067783941-51c9c23ea3a3', '1587293852726-70cdb56c2866')
WHERE media_url LIKE '%1534067783941-51c9c23ea3a3%';

UPDATE property_medias
SET 
    media_url = REPLACE(media_url, '1580587771525-78c9ad819599', '1512917774080-9991f1c4c750'),
    thumbnail_url = REPLACE(thumbnail_url, '1580587771525-78c9ad819599', '1512917774080-9991f1c4c750')
WHERE media_url LIKE '%1580587771525-78c9ad819599%';

UPDATE property_medias
SET 
    media_url = REPLACE(media_url, '1586528116311-ad861fbc2a4b', '1587293852726-70cdb56c2866'),
    thumbnail_url = REPLACE(thumbnail_url, '1586528116311-ad861fbc2a4b', '1587293852726-70cdb56c2866')
WHERE media_url LIKE '%1586528116311-ad861fbc2a4b%';
