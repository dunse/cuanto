-- Add TestCase.analysisCount
ALTER TABLE `test_case` ADD COLUMN `analysis_count` int(11);

-- Initialize TestCase.analysisCount
UPDATE test_case tc SET tc.analysis_count =
    (SELECT COUNT(*) FROM test_outcome tout, analysis_state anlz
          WHERE tout.test_case_id = tc.id AND anlz.is_analyzed IS TRUE AND tout.analysis_state_id = anlz.id);

-- Add TestRun.allowPurge
ALTER TABLE `test_run` ADD COLUMN `allow_purge` bit(1) default true;
