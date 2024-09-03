-- Rename the table quiz_mcq to attempt
ALTER TABLE qms_quiz.quiz_mcq RENAME TO attempt;

-- Add new columns option_no and attempt_time
ALTER TABLE qms_quiz.attempt
    ADD COLUMN option_no INT DEFAULT NULL,
    ADD COLUMN attempt_time TIMESTAMP DEFAULT NULL;
