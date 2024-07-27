-- src/main/resources/db/migration/dml/V2__insert_initial_data.sql
INSERT INTO qms_quiz.quiz (status, student_id) VALUES ('IN_PROGRESS', 'student123');
INSERT INTO qms_quiz.quiz_mcq (quiz_id, mcq_id) VALUES (1, 1);