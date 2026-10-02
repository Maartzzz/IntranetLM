ALTER TABLE course_materials
DROP CONSTRAINT IF EXISTS chk_course_material_type;

ALTER TABLE course_materials
ADD CONSTRAINT chk_course_material_type
CHECK (
    type IN (
        'VIDEO',
        'YOUTUBE',
        'PDF',
        'PPTX',
        'DOCUMENT',
        'URL'
    )
);