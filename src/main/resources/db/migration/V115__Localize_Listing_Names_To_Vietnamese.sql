-- V115__Localize_Listing_Names_To_Vietnamese.sql
-- Replace English property type prefixes in listing names and property descriptions with Vietnamese equivalents
-- To ensure linguistic consistency and improve listing data quality.

-- 1. Correct Listing Titles
UPDATE listings SET name = REPLACE(name, 'APARTMENT sang trọng', 'Căn hộ sang trọng') WHERE name LIKE 'APARTMENT sang trọng%';
UPDATE listings SET name = REPLACE(name, 'HOUSE sang trọng', 'Nhà riêng sang trọng') WHERE name LIKE 'HOUSE sang trọng%';
UPDATE listings SET name = REPLACE(name, 'VILLA sang trọng', 'Biệt thự sang trọng') WHERE name LIKE 'VILLA sang trọng%';
UPDATE listings SET name = REPLACE(name, 'TOWNHOUSE sang trọng', 'Nhà phố sang trọng') WHERE name LIKE 'TOWNHOUSE sang trọng%';
UPDATE listings SET name = REPLACE(name, 'PENTHOUSE sang trọng', 'Penthouse sang trọng') WHERE name LIKE 'PENTHOUSE sang trọng%';
UPDATE listings SET name = REPLACE(name, 'STUDIO sang trọng', 'Studio sang trọng') WHERE name LIKE 'STUDIO sang trọng%';

UPDATE listings SET name = REPLACE(name, 'OFFICE sang trọng', 'Văn phòng sang trọng') WHERE name LIKE 'OFFICE sang trọng%';
UPDATE listings SET name = REPLACE(name, 'SHOPHOUSE sang trọng', 'Shophouse sang trọng') WHERE name LIKE 'SHOPHOUSE sang trọng%';
UPDATE listings SET name = REPLACE(name, 'RETAIL sang trọng', 'Không gian bán lẻ sang trọng') WHERE name LIKE 'RETAIL sang trọng%';
UPDATE listings SET name = REPLACE(name, 'MALL sang trọng', 'Trung tâm thương mại sang trọng') WHERE name LIKE 'MALL sang trọng%';
UPDATE listings SET name = REPLACE(name, 'RESTAURANT sang trọng', 'Nhà hàng sang trọng') WHERE name LIKE 'RESTAURANT sang trọng%';
UPDATE listings SET name = REPLACE(name, 'HOTEL sang trọng', 'Khách sạn sang trọng') WHERE name LIKE 'HOTEL sang trọng%';

UPDATE listings SET name = REPLACE(name, 'WAREHOUSE sang trọng', 'Kho bãi sang trọng') WHERE name LIKE 'WAREHOUSE sang trọng%';
UPDATE listings SET name = REPLACE(name, 'FACTORY sang trọng', 'Nhà máy sang trọng') WHERE name LIKE 'FACTORY sang trọng%';
UPDATE listings SET name = REPLACE(name, 'WORKSHOP sang trọng', 'Xưởng sang trọng') WHERE name LIKE 'WORKSHOP sang trọng%';
UPDATE listings SET name = REPLACE(name, 'LOGISTICS sang trọng', 'Trung tâm Logistics sang trọng') WHERE name LIKE 'LOGISTICS sang trọng%';

UPDATE listings SET name = REPLACE(name, 'LAND_RESIDENTIAL sang trọng', 'Đất nhà ở sang trọng') WHERE name LIKE 'LAND_RESIDENTIAL sang trọng%';
UPDATE listings SET name = REPLACE(name, 'LAND_COMMERCIAL sang trọng', 'Đất thương mại sang trọng') WHERE name LIKE 'LAND_COMMERCIAL sang trọng%';
UPDATE listings SET name = REPLACE(name, 'LAND_INDUSTRIAL sang trọng', 'Đất công nghiệp sang trọng') WHERE name LIKE 'LAND_INDUSTRIAL sang trọng%';
UPDATE listings SET name = REPLACE(name, 'LAND_AGRICULTURAL sang trọng', 'Đất nông nghiệp sang trọng') WHERE name LIKE 'LAND_AGRICULTURAL sang trọng%';

-- 2. Correct Property Descriptions
UPDATE properties SET descriptions = REPLACE(descriptions, 'Tuyệt phẩm apartment', 'Tuyệt phẩm căn hộ') WHERE descriptions LIKE '%Tuyệt phẩm apartment%';
UPDATE properties SET descriptions = REPLACE(descriptions, 'Tuyệt phẩm house', 'Tuyệt phẩm nhà riêng') WHERE descriptions LIKE '%Tuyệt phẩm house%';
UPDATE properties SET descriptions = REPLACE(descriptions, 'Tuyệt phẩm villa', 'Tuyệt phẩm biệt thự') WHERE descriptions LIKE '%Tuyệt phẩm villa%';
UPDATE properties SET descriptions = REPLACE(descriptions, 'Tuyệt phẩm townhouse', 'Tuyệt phẩm nhà phố') WHERE descriptions LIKE '%Tuyệt phẩm townhouse%';
-- Penthouse and Studio are the same in Vietnamese
UPDATE properties SET descriptions = REPLACE(descriptions, 'Tuyệt phẩm office', 'Tuyệt phẩm văn phòng') WHERE descriptions LIKE '%Tuyệt phẩm office%';
-- Shophouse is the same 
UPDATE properties SET descriptions = REPLACE(descriptions, 'Tuyệt phẩm retail', 'Tuyệt phẩm không gian bán lẻ') WHERE descriptions LIKE '%Tuyệt phẩm retail%';
UPDATE properties SET descriptions = REPLACE(descriptions, 'Tuyệt phẩm mall', 'Tuyệt phẩm trung tâm thương mại') WHERE descriptions LIKE '%Tuyệt phẩm mall%';
UPDATE properties SET descriptions = REPLACE(descriptions, 'Tuyệt phẩm restaurant', 'Tuyệt phẩm nhà hàng') WHERE descriptions LIKE '%Tuyệt phẩm restaurant%';
UPDATE properties SET descriptions = REPLACE(descriptions, 'Tuyệt phẩm hotel', 'Tuyệt phẩm khách sạn') WHERE descriptions LIKE '%Tuyệt phẩm hotel%';

UPDATE properties SET descriptions = REPLACE(descriptions, 'Tuyệt phẩm warehouse', 'Tuyệt phẩm kho bãi') WHERE descriptions LIKE '%Tuyệt phẩm warehouse%';
UPDATE properties SET descriptions = REPLACE(descriptions, 'Tuyệt phẩm factory', 'Tuyệt phẩm nhà máy') WHERE descriptions LIKE '%Tuyệt phẩm factory%';
UPDATE properties SET descriptions = REPLACE(descriptions, 'Tuyệt phẩm workshop', 'Tuyệt phẩm xưởng') WHERE descriptions LIKE '%Tuyệt phẩm workshop%';
UPDATE properties SET descriptions = REPLACE(descriptions, 'Tuyệt phẩm logistics', 'Tuyệt phẩm trung tâm Logistics') WHERE descriptions LIKE '%Tuyệt phẩm logistics%';

UPDATE properties SET descriptions = REPLACE(descriptions, 'Tuyệt phẩm land_residential', 'Tuyệt phẩm đất nhà ở') WHERE descriptions LIKE '%Tuyệt phẩm land_residential%';
UPDATE properties SET descriptions = REPLACE(descriptions, 'Tuyệt phẩm land_commercial', 'Tuyệt phẩm đất thương mại') WHERE descriptions LIKE '%Tuyệt phẩm land_commercial%';
UPDATE properties SET descriptions = REPLACE(descriptions, 'Tuyệt phẩm land_industrial', 'Tuyệt phẩm đất công nghiệp') WHERE descriptions LIKE '%Tuyệt phẩm land_industrial%';
UPDATE properties SET descriptions = REPLACE(descriptions, 'Tuyệt phẩm land_agricultural', 'Tuyệt phẩm đất nông nghiệp') WHERE descriptions LIKE '%Tuyệt phẩm land_agricultural%';
