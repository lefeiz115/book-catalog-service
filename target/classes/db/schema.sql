-- ============================================================
-- Book Catalog Service - Database DDL Scripts
-- Target Database: MySQL 8.0+
-- Engine: InnoDB (supports transactions, row-level locking, foreign keys)
-- Charset: utf8mb4 (full Unicode support, including emoji)
-- ============================================================

-- ============================================================
-- 1. Create Database
-- ============================================================
CREATE DATABASE IF NOT EXISTS `bookcatalog`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `bookcatalog`;

-- ============================================================
-- 2. Drop existing tables (for clean re-deployment)
-- ============================================================
DROP TABLE IF EXISTS `t_book`;

-- ============================================================
-- 3. Create Table: t_book
-- ============================================================
CREATE TABLE `t_book` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT          COMMENT 'Primary key',
    `title`         VARCHAR(500) NOT NULL                         COMMENT 'Book title',
    `author`        VARCHAR(200) NOT NULL                         COMMENT 'Author name',
    `isbn`          VARCHAR(20)  DEFAULT NULL                     COMMENT 'ISBN number',
    `genre`         VARCHAR(50)  DEFAULT NULL                     COMMENT 'Book genre (e.g. Fiction, Technology)',
    `price`         DECIMAL(10,2) DEFAULT NULL                    COMMENT 'Book price',
    `description`   VARCHAR(2000) DEFAULT NULL                    COMMENT 'Book description',
    `stock`         INT          NOT NULL DEFAULT 0               COMMENT 'Stock quantity',
    `pages`         INT          NOT NULL DEFAULT 0               COMMENT 'Number of pages',
    `publisher`     VARCHAR(50)  DEFAULT NULL                     COMMENT 'Publisher name',
    `publish_date`  DATETIME     DEFAULT NULL                     COMMENT 'Publication date',
    `active`        TINYINT(1)   NOT NULL DEFAULT 1               COMMENT 'Active status: 1=active, 0=inactive',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation time',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
    `version`       BIGINT       DEFAULT 0                        COMMENT 'Optimistic locking version',
    PRIMARY KEY (`id`),
    INDEX `idx_book_title`  (`title`),
    INDEX `idx_book_author` (`author`),
    INDEX `idx_book_genre`  (`genre`),
    INDEX `idx_book_isbn`   (`isbn`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Book catalog table';

-- ============================================================
-- 4. (Optional) Create unique index on ISBN
--    Note: The JPA entity uses a non-unique index on ISBN to
--          allow null values and flexible updates. Enable this
--          if strict ISBN uniqueness is required.
-- ============================================================
-- ALTER TABLE `t_book` ADD UNIQUE INDEX `uk_book_isbn` (`isbn`);

-- ============================================================
-- 5. Insert sample data
-- ============================================================
INSERT INTO `t_book` (`title`, `author`, `isbn`, `genre`, `price`, `description`, `stock`, `pages`, `publisher`, `publish_date`, `active`)
VALUES
    ('Clean Code: A Handbook of Agile Software Craftsmanship', 'Robert C. Martin', '978-0132350884', 'TECHNOLOGY', 39.99, 'Even bad code can function. But if code isn''t clean, it can bring a development organization to its knees.', 50, 464, 'Prentice Hall', '2008-08-01 00:00:00', 1),
    ('The Pragmatic Programmer: Your Journey to Mastery', 'David Thomas', '978-0135957059', 'TECHNOLOGY', 49.99, 'A classic book for programmers that covers everything from career advice to coding techniques.', 35, 352, 'Addison-Wesley Professional', '2019-09-13 00:00:00', 1),
    ('Design Patterns: Elements of Reusable Object-Oriented Software', 'Erich Gamma', '978-0201633610', 'TECHNOLOGY', 54.99, 'Capturing a wealth of experience about the design of object-oriented software.', 25, 395, 'Addison-Wesley Professional', '1994-10-31 00:00:00', 1),
    ('Effective Java', 'Joshua Bloch', '978-0134685991', 'TECHNOLOGY', 45.00, 'The definitive guide to Java programming best practices.', 60, 416, 'Addison-Wesley Professional', '2018-01-06 00:00:00', 1),
    ('Spring in Action, Sixth Edition', 'Craig Walls', '978-1617294945', 'TECHNOLOGY', 44.99, 'A fully revised edition of the classic Spring framework guide.', 40, 520, 'Manning Publications', '2022-04-12 00:00:00', 1),
    ('The Lord of the Rings', 'J.R.R. Tolkien', '978-0544003415', 'FANTASY', 25.99, 'An epic high fantasy novel about the quest to destroy the One Ring.', 100, 1178, 'Houghton Mifflin Harcourt', '2012-09-18 00:00:00', 1),
    ('1984', 'George Orwell', '978-0451524935', 'FICTION', 9.99, 'A dystopian social science fiction novel and cautionary tale.', 200, 328, 'Signet Classic', '1961-01-01 00:00:00', 1),
    ('To Kill a Mockingbird', 'Harper Lee', '978-0061120084', 'FICTION', 12.99, 'A novel about racial injustice and loss of innocence in the American South.', 80, 384, 'Harper Perennial', '2006-05-23 00:00:00', 1),
    ('Sapiens: A Brief History of Humankind', 'Yuval Noah Harari', '978-0062316097', 'HISTORY', 18.99, 'A sweeping narrative of human history from the Stone Age to the modern era.', 75, 464, 'Harper', '2015-02-10 00:00:00', 1),
    ('The Selfish Gene', 'Richard Dawkins', '978-0198788607', 'SCIENCE', 16.99, 'A revolutionary book that changed the way we think about evolution.', 45, 496, 'Oxford University Press', '2016-04-21 00:00:00', 1),
    ('Atomic Habits', 'James Clear', '978-0735211292', 'SELF_HELP', 23.00, 'An easy and proven way to build good habits and break bad ones.', 120, 320, 'Avery', '2018-10-16 00:00:00', 1),
    ('The Art of Computer Programming, Vol. 1', 'Donald E. Knuth', '978-0201896831', 'TECHNOLOGY', 69.99, 'The seminal work on algorithms and computer programming.', 15, 672, 'Addison-Wesley Professional', '1997-10-15 00:00:00', 0);

-- ============================================================
-- 6. Verify data
-- ============================================================
SELECT COUNT(*) AS total_books FROM `t_book`;
SELECT genre, COUNT(*) AS count FROM `t_book` GROUP BY genre ORDER BY count DESC;
