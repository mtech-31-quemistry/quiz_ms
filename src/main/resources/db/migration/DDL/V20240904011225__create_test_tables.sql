ALTER TABLE qms_quiz.attempt RENAME TO quiz_attempt;

CREATE TABLE qms_quiz.test (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255),
    tutor_id VARCHAR(255),
    created_on TIMESTAMP,
    updated_on TIMESTAMP
);

CREATE TABLE qms_quiz.test_mcq (
    test_id BIGINT,
    mcq_id BIGINT,
    index INT,
    PRIMARY KEY (test_id, mcq_id)
);

CREATE TABLE qms_quiz.test_student (
    test_id BIGINT,
    student_id VARCHAR(255),
    points INT,
    PRIMARY KEY (test_id, student_id)
);

CREATE TABLE qms_quiz.test_attempt (
    test_id BIGINT,
    student_id VARCHAR(255),
    mcq_id BIGINT,
    option_no INT DEFAULT NULL,
    attempt_time TIMESTAMP DEFAULT NULL,
    PRIMARY KEY (test_id, student_id, mcq_id)
);
