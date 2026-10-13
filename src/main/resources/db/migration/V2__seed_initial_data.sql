-- V2: Seed initial categories, books, and default users

-- Default users (Password is 'Password123!')
-- Default user seed
INSERT INTO users (id, email, password_hash, full_name, role, gift_points)
VALUES 
  ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'customer@bookworm.example.com', '$2a' || '$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'John Smith', 'ROLE_CUSTOMER', 100),
  ('a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'admin@bookworm.example.com', '$2a' || '$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Platform Admin', 'ROLE_ADMIN', 500)
ON CONFLICT (email) DO NOTHING;

-- Seed Categories (Wireframe left sidebar categories)
INSERT INTO categories (id, name, slug) VALUES
  ('b1a1a1a1-1111-4111-8111-111111111111', 'Romance', 'romance'),
  ('b2a2a2a2-2222-4222-8222-222222222222', 'Mystery', 'mystery'),
  ('b3a3a3a3-3333-4333-8333-333333333333', 'Science Fiction', 'science-fiction'),
  ('b4a4a4a4-4444-4444-8444-444444444444', 'Fantasy', 'fantasy'),
  ('b5a5a5a5-5555-4555-8555-555555555555', 'Historical', 'historical'),
  ('b6a6a6a6-6666-4666-8666-666666666666', 'Biography', 'biography'),
  ('b7a7a7a7-7777-4777-8777-777777777777', 'Self Help', 'self-help'),
  ('b8a8a8a8-8888-4888-8888-888888888888', 'Memoir', 'memoir'),
  ('b9a9a9a9-9999-4999-8999-999999999999', 'Travel', 'travel'),
  ('baa1a1a1-aaaa-4aaa-8aaa-aaaaaaaaaaaa', 'Cooking', 'cooking'),
  ('bbb2b2b2-bbbb-4bbb-8bbb-bbbbbbbbbbbb', 'Children''s', 'childrens'),
  ('bcc3c3c3-cccc-4ccc-8ccc-cccccccccccc', 'Young Adult', 'young-adult'),
  ('bdd4d4d4-dddd-4ddd-8ddd-dddddddddddd', 'Comics & Graphic Novels', 'comics-graphic-novels'),
  ('bee5e5e5-eeee-4eee-8eee-eeeeeeeeeeee', 'Poetry', 'poetry'),
  ('bff6f6f6-ffff-4fff-8fff-ffffffffffff', 'Drama', 'drama'),
  ('c0070707-0000-4000-8000-000000000000', 'Science', 'science'),
  ('c1181818-1111-4111-8111-111111111111', 'Philosophy', 'philosophy'),
  ('c2292929-2222-4222-8222-222222222222', 'Religion', 'religion'),
  ('c33a3a3a-3333-4333-8333-333333333333', 'Language Learning', 'language-learning')
ON CONFLICT (name) DO NOTHING;

-- Seed Books matching presentation wireframes
INSERT INTO books (id, title, isbn, description, category_id, author_name, author_bio, author_avatar_url, publisher_name, format, language, price, stock_quantity, sales_count, rating, front_cover_url, back_cover_url, is_bestseller, is_new_launch, published_date)
VALUES
  ('c1c1c1c1-1111-4111-8111-111111111111', 'The Art of Focus', '978-0-111111-11-1', 
   'Practical guide to mastering focus & boosting productivity every day.', 
   'b7a7a7a7-7777-4777-8777-777777777777', 'Arjun Patel', 
   'Arjun Patel is a seasoned researcher and productivity specialist.', 
   'https://assets.bookworm.example/authors/arjun-patel.jpg', 'ABC Publishers', 
   'Paperback', 'English', 399.00, 120, 210, 4.8, 
   'https://assets.bookworm.example/covers/art-of-focus-front.jpg', 
   'https://assets.bookworm.example/covers/art-of-focus-back.jpg', 
   false, false, '2025-06-15'),

  ('c2c2c2c2-2222-4222-8222-222222222222', 'Joy of Minimalism', '978-0-123456-78-9', 
   'Declutter your life to uncover peace, clarity, and joy.', 
   'b7a7a7a7-7777-4777-8777-777777777777', 'Daniel Reed', 
   'Daniel Reed is a writer, minimalist, and productivity coach based in San Francisco.', 
   'https://assets.bookworm.example/authors/daniel-reed.jpg', 'ABC Publishers', 
   'Paperback', 'English', 149.00, 85, 145, 4.6, 
   'https://assets.bookworm.example/covers/joy-of-minimalism-front.jpg', 
   'https://assets.bookworm.example/covers/joy-of-minimalism-back.jpg', 
   false, true, '2026-01-10'),

  ('c3c3c3c3-3333-4333-8333-333333333333', 'The Midnight Hour', '978-0-333333-33-3', 
   'Haunting tale of a man''s journey & the shadows of a forgotten past.', 
   'b2a2a2a2-2222-4222-8222-222222222222', 'James Adams', 
   'James Adams writes award-winning mysteries and psychological thrillers.', 
   'https://assets.bookworm.example/authors/james-adams.jpg', 'ABC Publishers', 
   'Paperback', 'English', 299.00, 60, 312, 4.7, 
   'https://assets.bookworm.example/covers/midnight-hour-front.jpg', 
   'https://assets.bookworm.example/covers/midnight-hour-back.jpg', 
   true, false, '2025-09-01'),

  ('c4c4c4c4-4444-4444-8444-444444444444', 'The Path to Success', '978-0-444444-44-4', 
   'A practical guide to achieving goals with clarity and confidence.', 
   'b7a7a7a7-7777-4777-8777-777777777777', 'James Wright', 
   'James Wright is an executive leadership coach and bestselling author.', 
   'https://assets.bookworm.example/authors/james-wright.jpg', 'ABC Publishers', 
   'Paperback', 'English', 359.00, 95, 180, 4.5, 
   'https://assets.bookworm.example/covers/path-to-success-front.jpg', 
   'https://assets.bookworm.example/covers/path-to-success-back.jpg', 
   false, false, '2025-11-20'),

  ('c5c5c5c5-5555-4555-8555-555555555555', 'Beneath the Stars', '978-0-555555-55-5', 
   'A heartwarming tale, where two souls discover who you need.', 
   'b1a1a1a1-1111-4111-8111-111111111111', 'Jessica Martin', 
   'Jessica Martin writes romantic fiction celebrated worldwide.', 
   'https://assets.bookworm.example/authors/jessica-martin.jpg', 'ABC Publishers', 
   'Hardcover', 'English', 499.00, 40, 420, 4.9, 
   'https://assets.bookworm.example/covers/beneath-the-stars-front.jpg', 
   'https://assets.bookworm.example/covers/beneath-the-stars-back.jpg', 
   true, false, '2025-08-14'),

  ('c6c6c6c6-6666-4666-8666-666666666666', 'The Final Frontier', '978-0-666666-66-6', 
   'A mission to space secrets to change humanity forever.', 
   'b3a3a3a3-3333-4333-8333-333333333333', 'Laura Mitchell', 
   'Laura Mitchell is a sci-fi novelist and aerospace engineer.', 
   'https://assets.bookworm.example/authors/laura-mitchell.jpg', 'ABC Publishers', 
   'Paperback', 'English', 359.00, 75, 290, 4.6, 
   'https://assets.bookworm.example/covers/final-frontier-front.jpg', 
   'https://assets.bookworm.example/covers/final-frontier-back.jpg', 
   true, false, '2025-10-05')
ON CONFLICT (isbn) DO NOTHING;

-- Seed Sample Review for Joy of Minimalism
INSERT INTO reviews (id, book_id, user_id, user_name, rating, comment)
VALUES (
  'e1e1e1e1-1111-4111-8111-111111111111', 
  'c2c2c2c2-2222-4222-8222-222222222222', 
  'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 
  'John Smith', 
  5, 
  'The accordion component delivers large amounts of content in a small space through progressive disclosure. The user gets key details about the underlying content.'
)
ON CONFLICT (id) DO NOTHING;
