ALTER TABLE qms_quiz.test_attempt
    ADD COLUMN is_correct BOOLEAN DEFAULT FALSE;

ALTER TABLE qms_quiz.quiz_attempt
    ADD COLUMN is_correct BOOLEAN DEFAULT FALSE;