ALTER TABLE prices RENAME TO prices_old;
CREATE TABLE prices
(
    date  DATE NOT NULL,
    price DECIMAL(16, 8),
    chain VARCHAR(255) NOT NULL,
    PRIMARY KEY(chain, date)
);
INSERT INTO prices (date, price, chain) SELECT date, price, 'BTC' FROM prices_old;
DROP TABLE prices_old;