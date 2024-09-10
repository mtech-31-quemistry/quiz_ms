ALTER TABLE qms_quiz.test
    ADD COLUMN created_by VARCHAR(255),
    ADD COLUMN started_by VARCHAR(255),
    ADD COLUMN started_on TIMESTAMP,
    ADD COLUMN completed_by VARCHAR(255),
    ADD COLUMN completed_on TIMESTAMP;