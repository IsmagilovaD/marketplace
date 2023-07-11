CREATE TYPE user_type AS ENUM ('Seller' ,'Buyer');
CREATE TABLE users
(
    id           SERIAL PRIMARY KEY NOT NULL,
    username     VARCHAR(255),
    password     VARCHAR(255)       NOT NULL,
    phone_number VARCHAR(20),
    email        VARCHAR(255),
    user_type    user_type,
    CONSTRAINT phone_or_email_not_null CHECK ( phone_number IS NOT NULL OR email IS NOT NULL)
);

CREATE TABLE bonus_accounts
(
    account_number SERIAL PRIMARY KEY NOT NULL,
    bonus_amount   INTEGER,
    user_id        INTEGER,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE buyers
(
    id      SERIAL PRIMARY KEY NOT NULL,
    user_id INTEGER,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE shopping_carts
(
    id       SERIAL PRIMARY KEY NOT NULL,
    buyer_id INTEGER,
    FOREIGN KEY (buyer_id) REFERENCES buyers (id)
);

CREATE TABLE sellers
(
    id      SERIAL PRIMARY KEY NOT NULL,
    user_id INTEGER,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TYPE product_category AS ENUM ('Electronics' ,'Clothing', 'Footwear',
    'BabyProducts', 'BeautyAndHealth', 'Appliances',
    'SportsAndLeisure', 'PetSupplies', 'Books');

CREATE TABLE products
(
    id          SERIAL PRIMARY KEY NOT NULL,
    seller_id   INTEGER,
    name        VARCHAR(255),
    price       INTEGER,
    category    product_category,
    description TEXT,
    image       VARCHAR(255),
    FOREIGN KEY (seller_id) REFERENCES sellers (id)
);

CREATE TABLE orders
(
    id               SERIAL PRIMARY KEY NOT NULL,
    buyer_id         INTEGER,
    order_date       BIGINT,
    total_price      INTEGER,
    shipping_address VARCHAR(255),
    FOREIGN KEY (buyer_id) REFERENCES buyers (id)

);

CREATE TYPE order_status AS ENUM ('New', 'InDelivery', 'Received', 'Cancelled');
CREATE TABLE order_items
(
    id         SERIAL PRIMARY KEY NOT NULL,
    product_id INTEGER,
    order_id   INTEGER,
    status     order_status,
    price      INTEGER,
    quantity   SMALLINT,
    FOREIGN KEY (product_id) REFERENCES products (id),
    FOREIGN KEY (order_id) REFERENCES orders (id)
);

CREATE TABLE cart_products
(
    shopping_cart_id INTEGER,
    product_id       INTEGER,
    quantity         SMALLINT,
    FOREIGN KEY (shopping_cart_id) REFERENCES shopping_carts (id),
    FOREIGN KEY (product_id) REFERENCES products (id),
    PRIMARY KEY (shopping_cart_id, product_id)
);

CREATE TABLE seller_order_item
(
    seller_id     INTEGER,
    order_item_id INTEGER,
    FOREIGN KEY (seller_id) REFERENCES sellers (id),
    FOREIGN KEY (order_item_id) REFERENCES order_items (id),
    PRIMARY KEY (seller_id, order_item_id)
);

