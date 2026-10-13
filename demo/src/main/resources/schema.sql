-- レッスン11：書籍テーブル
CREATE TABLE IF NOT EXISTS books (
    id     SERIAL PRIMARY KEY,
    title  VARCHAR(100) NOT NULL,
    author VARCHAR(50)  NOT NULL,
    price  INTEGER      NOT NULL
);
