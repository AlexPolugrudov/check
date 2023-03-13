ALTER TABLE budget
ALTER COLUMN type TYPE varchar(20)
    USING CASE type WHEN 'Комиссия' THEN 'Расход' ELSE type END;