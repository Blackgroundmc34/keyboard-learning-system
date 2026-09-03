-- Idempotent development data for exercising the student learning flow.
-- Running this file again updates the same course and lessons without deleting data.

INSERT INTO courses (
  title,
  slug,
  description,
  level,
  estimated_duration_minutes,
  is_published
)
VALUES (
  'Keyboard Fundamentals',
  'keyboard-fundamentals',
  'Learn keyboard layout, posture, and foundational playing technique.',
  'BEGINNER',
  60,
  1
)
ON DUPLICATE KEY UPDATE
  id = LAST_INSERT_ID(id),
  title = VALUES(title),
  description = VALUES(description),
  level = VALUES(level),
  estimated_duration_minutes = VALUES(estimated_duration_minutes),
  is_published = VALUES(is_published);

SET @course_id = LAST_INSERT_ID();

INSERT INTO lessons (
  course_id,
  title,
  slug,
  description,
  content,
  lesson_order,
  duration_minutes,
  is_preview,
  is_published
)
VALUES (
  @course_id,
  'Introduction to the Keyboard',
  'introduction-to-the-keyboard',
  'Identify the main sections of a keyboard.',
  'Learn the white keys, black keys, and repeating note pattern.',
  1,
  20,
  1,
  1
)
ON DUPLICATE KEY UPDATE
  id = LAST_INSERT_ID(id),
  title = VALUES(title),
  description = VALUES(description),
  content = VALUES(content),
  lesson_order = VALUES(lesson_order),
  duration_minutes = VALUES(duration_minutes),
  is_preview = VALUES(is_preview),
  is_published = VALUES(is_published);

SET @first_lesson_id = LAST_INSERT_ID();

INSERT INTO lesson_materials (
  lesson_id,
  material_type,
  title,
  text_content,
  material_order,
  is_downloadable
)
SELECT
  @first_lesson_id,
  'TEXT',
  'Keyboard layout notes',
  'Groups of two and three black keys help identify notes across the keyboard.',
  1,
  0
WHERE NOT EXISTS (
  SELECT 1
  FROM lesson_materials
  WHERE lesson_id = @first_lesson_id
    AND title = 'Keyboard layout notes'
);

INSERT INTO lessons (
  course_id,
  title,
  slug,
  description,
  content,
  lesson_order,
  duration_minutes,
  is_preview,
  is_published
)
VALUES (
  @course_id,
  'Posture and Hand Position',
  'posture-and-hand-position',
  'Develop a relaxed and sustainable playing position.',
  'Sit upright, relax the shoulders, and keep the wrists neutral.',
  2,
  20,
  0,
  1
)
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  description = VALUES(description),
  content = VALUES(content),
  lesson_order = VALUES(lesson_order),
  duration_minutes = VALUES(duration_minutes),
  is_preview = VALUES(is_preview),
  is_published = VALUES(is_published);

INSERT INTO lessons (
  course_id,
  title,
  slug,
  description,
  content,
  lesson_order,
  duration_minutes,
  is_preview,
  is_published
)
VALUES (
  @course_id,
  'Playing Your First Notes',
  'playing-your-first-notes',
  'Apply the keyboard layout and hand position.',
  'Play a short five-note pattern slowly and evenly.',
  3,
  20,
  0,
  1
)
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  description = VALUES(description),
  content = VALUES(content),
  lesson_order = VALUES(lesson_order),
  duration_minutes = VALUES(duration_minutes),
  is_preview = VALUES(is_preview),
  is_published = VALUES(is_published);
