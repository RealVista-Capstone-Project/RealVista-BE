-- V62__Seed_3d_media_for_property_and_listing.sql
-- Seeds two completed 3D generation records for property a1100000-0000-0000-0000-000000000001
-- and links their media to listing 27199eda-c29e-7a94-c7cc-93959e8115cc

DO $$
DECLARE
    v_property_id   UUID := 'a1100000-0000-0000-0000-000000000001';
    v_listing_id    UUID := '27199eda-c29e-7a94-c7cc-93959e8115cc';
    v_uploader_id   UUID := '550e8400-e29b-41d4-a716-446655440101';
BEGIN

    -- ============================================================================
    -- Record 1: Okelahi
    -- ============================================================================

    INSERT INTO property_3d_generations (
        id, property_id, uploader_id, operation_id,
        status, room_name, error_message,
        created_at, updated_at, deleted, version
    )
    VALUES (
        'b138b5cc-f934-4c0c-b96a-1703aec37414',
        v_property_id,
        v_uploader_id,
        '071d1de7-ba1c-4fe8-9703-8fa188d192dc',
        'SUCCEEDED', 'Okelahi', NULL,
        '2026-04-07 16:11:41.911+07', '2026-04-07 20:17:35.434+07', FALSE, 0
    )
    ON CONFLICT (id) DO NOTHING;

    INSERT INTO property_medias (
        property_media_id, property_id, upload_by, media_type,
        media_url, thumbnail_url,
        is_primary, is_property_standard,
        metadata, created_at, updated_at, deleted
    )
    VALUES (
        'a0e1034f-c798-4bbd-aea9-22c4b0b64861',
        v_property_id,
        v_uploader_id,
        '3D',
        'https://cdn.marble.worldlabs.ai/9844bc52-6db4-48cc-a8e5-9565b9258fbe/61049f4a-0374-4554-8b91-09fd686656ab_sand.spz',
        'https://cdn.marble.worldlabs.ai/9844bc52-6db4-48cc-a8e5-9565b9258fbe/882cefac-5a67-49e7-ac0f-a36b08d55c08_sand_mpi/thumbnail.webp',
        FALSE, TRUE,
        '{"room_name":"Okelahi","operation_id":"071d1de7-ba1c-4fe8-9703-8fa188d192dc","marble_assets":{"mesh":{"collider_mesh_url":"https://cdn.marble.worldlabs.ai/9844bc52-6db4-48cc-a8e5-9565b9258fbe/a4e9c733.glb"},"splats":{"spz_urls":{"100k":"https://cdn.marble.worldlabs.ai/9844bc52-6db4-48cc-a8e5-9565b9258fbe/0d60f532-f78a-40e4-8a69-a6ae42f1de26_sand_100k.spz","500k":"https://cdn.marble.worldlabs.ai/9844bc52-6db4-48cc-a8e5-9565b9258fbe/45a9d46d-3e4c-4f17-b439-34243e0a144e_sand_500k.spz","full_res":"https://cdn.marble.worldlabs.ai/9844bc52-6db4-48cc-a8e5-9565b9258fbe/61049f4a-0374-4554-8b91-09fd686656ab_sand.spz"},"semantics_metadata":null},"caption":"The scene is a lived-in apartment interior, captured in a realistic style with a candid and somewhat cluttered tone, reflecting daily life. The overall atmosphere is unpretentious and functional, typical of a busy living space. A cream-colored mattress with a floral pattern rests on the floor to the right, next to another mattress striped in gray and white. A large white fan stands next to the floral mattress, its blades visible. A small white pillow and a folded grey blanket are on the striped mattress. Further into the room, a wooden desk with a black computer monitor and a grey office chair is visible, indicating a workspace. Several boxes and luggage are stacked around the desk and in the background, suggesting either recent moving or a general storage area. The walls are painted a light cream color, and the floor is covered in light-colored tiles. A wooden door is located at the far end of the room, with a dark mat in front of it. On the wall to the left, there are two circular light fixtures. A small, dark red bin is on top of a brown wooden shelving unit. The mattresses are positioned on the floor towards the right side of the room, with the fan next to the floral one. The desk and office chair are situated to the left of the mattresses, against the back wall. The wooden door is centrally located at the far end of the room, with the stacked boxes and luggage around it. The shelving unit is to the left of the door, and the light fixtures are along the left wall. The 360 scene is faultless.","imagery":{"pano_url":null},"thumbnail_url":"https://cdn.marble.worldlabs.ai/9844bc52-6db4-48cc-a8e5-9565b9258fbe/882cefac-5a67-49e7-ac0f-a36b08d55c08_sand_mpi/thumbnail.webp"}}'::jsonb,
        '2026-04-07 16:12:25.970', '2026-04-07 20:17:35.435', FALSE
    )
    ON CONFLICT (property_media_id) DO NOTHING;

    INSERT INTO listing_medias (
        listing_id, property_media_id,
        display_order, is_primary,
        created_at, updated_at, deleted
    )
    VALUES (
        v_listing_id,
        'a0e1034f-c798-4bbd-aea9-22c4b0b64861',
        100, FALSE,
        NOW(), NOW(), FALSE
    )
    ON CONFLICT (listing_id, property_media_id) DO NOTHING;

    -- ============================================================================
    -- Record 2: Test2
    -- ============================================================================

    INSERT INTO property_3d_generations (
        id, property_id, uploader_id, operation_id,
        status, room_name, error_message,
        created_at, updated_at, deleted, version
    )
    VALUES (
        '7b040d7c-4a0f-40c2-a3e2-d93d3664d14a',
        v_property_id,
        v_uploader_id,
        'b4336bff-98f3-4e99-9e22-ba14d5ce7df6',
        'SUCCEEDED', 'Test2', NULL,
        '2026-04-09 14:09:28.203+07', '2026-04-09 14:10:30.933+07', FALSE, 0
    )
    ON CONFLICT (id) DO NOTHING;

    INSERT INTO property_medias (
        property_media_id, property_id, upload_by, media_type,
        media_url, thumbnail_url,
        is_primary, is_property_standard,
        metadata, created_at, updated_at, deleted
    )
    VALUES (
        '534c6c9e-be09-4d43-a52e-ca6f2698ebd8',
        v_property_id,
        v_uploader_id,
        '3D',
        'https://cdn.marble.worldlabs.ai/416792d4-3bca-4a57-b5d1-e7a748036cf3/ecf35edb-c668-4dc2-b637-241ad61dd054_sand.spz',
        'https://cdn.marble.worldlabs.ai/416792d4-3bca-4a57-b5d1-e7a748036cf3/f3835415-e55b-4e30-a04a-94fa8cd77569_sand_mpi/thumbnail.webp',
        FALSE, TRUE,
        '{"room_name":"Test2","operation_id":"b4336bff-98f3-4e99-9e22-ba14d5ce7df6","marble_assets":{"mesh":{"collider_mesh_url":"https://cdn.marble.worldlabs.ai/416792d4-3bca-4a57-b5d1-e7a748036cf3/786eddeb.glb"},"splats":{"spz_urls":{"100k":"https://cdn.marble.worldlabs.ai/416792d4-3bca-4a57-b5d1-e7a748036cf3/289c8722-1406-4747-884e-cc8d0e04d8e4_sand_100k.spz","500k":"https://cdn.marble.worldlabs.ai/416792d4-3bca-4a57-b5d1-e7a748036cf3/f0861575-b3fd-4faf-8f34-105904d752de_sand_500k.spz","full_res":"https://cdn.marble.worldlabs.ai/416792d4-3bca-4a57-b5d1-e7a748036cf3/ecf35edb-c668-4dc2-b637-241ad61dd054_sand.spz"},"semantics_metadata":null},"caption":"The scene is a vibrant and bustling cafe interior, captured in a realistic style, showcasing a comfortable and inviting atmosphere ideal for work or relaxation. The overall tone is casual and productive, with warm lighting and a mix of textures. The cafe features various seating arrangements, including tables with dark chairs, some upholstered in brown leather, and others with plain wooden seats. A prominent feature is a large white brick wall with arched alcoves, functioning as bookshelves filled with numerous books, adding an academic and cozy touch. Overhead, exposed ductwork and track lighting contribute to the industrial-chic aesthetic, while decorative pendant lights with clear glass shades provide localized illumination. Large windows and glass doors allow natural light to filter into the space, offering glimpses of the exterior, which includes lush greenery and other buildings. The flooring is a mosaic of patterned tiles in warm earthy tones, complementing the overall color scheme. A large painting depicting scientific apparatus and candles hangs on one wall, adding an artistic focal point. Air conditioning units are mounted high on the walls, ensuring a comfortable environment. Scattered throughout the cafe are personal items such as backpacks, notebooks, and drinks, indicating the cafe''s use as a workspace. A wooden counter with various coffee-making equipment and shelves filled with products is visible in one area, suggesting a service station. To the left, the white brick wall extends, adorned with additional shelves holding more books, creating a continuous display of literature. The patterned tile floor covers the entire space, leading towards the back of the cafe where the counter area is located. The 360 scene is faultless.","imagery":{"pano_url":null},"thumbnail_url":"https://cdn.marble.worldlabs.ai/416792d4-3bca-4a57-b5d1-e7a748036cf3/f3835415-e55b-4e30-a04a-94fa8cd77569_sand_mpi/thumbnail.webp"}}'::jsonb,
        '2026-04-09 14:10:30.926', '2026-04-09 14:10:30.926', FALSE
    )
    ON CONFLICT (property_media_id) DO NOTHING;

    INSERT INTO listing_medias (
        listing_id, property_media_id,
        display_order, is_primary,
        created_at, updated_at, deleted
    )
    VALUES (
        v_listing_id,
        '534c6c9e-be09-4d43-a52e-ca6f2698ebd8',
        101, FALSE,
        NOW(), NOW(), FALSE
    )
    ON CONFLICT (listing_id, property_media_id) DO NOTHING;

END $$;
