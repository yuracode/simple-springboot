-- レッスン11：サンプルデータ（テーブルが空のときだけ入れる）
INSERT INTO books (title, author, price)
SELECT * FROM (VALUES
    ('スッキリわかるJava入門', '中山清喬', 2860),
    ('スッキリわかるサーブレット&JSP入門', '国本大悟', 3080),
    ('スッキリわかるSQL入門', '中山清喬', 2860),
    ('Spring徹底入門', 'NTTデータ', 4400)
) AS v(title, author, price)
WHERE NOT EXISTS (SELECT 1 FROM books);
