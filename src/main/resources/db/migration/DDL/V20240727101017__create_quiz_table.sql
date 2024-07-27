CREATE SCHEMA IF NOT EXISTS qms_quiz;

-- src/main/resources/db/migration/ddl/V1__create_quiz_table.sql
CREATE TABLE qms_quiz.quiz (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255),
    student_id VARCHAR(255),
    created_on TIMESTAMP,
    updated_on TIMESTAMP
);

CREATE TABLE qms_quiz.quiz_mcq (
    quiz_id BIGINT,
    mcq_id BIGINT,
    PRIMARY KEY (quiz_id, mcq_id)
);
