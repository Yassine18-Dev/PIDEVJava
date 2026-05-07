-- ========================================
-- BASE DE DONNÉES PIDEV JAVA
-- Module complet de gestion de produits gaming
-- ========================================

-- Création de la base de données
CREATE DATABASE IF NOT EXISTS pidev_java CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pidev_java;

-- ========================================
-- TABLE DES UTILISATEURS
-- ========================================
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- ========================================
-- TABLE DES PRODUITS
-- ========================================
CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    type ENUM('skin', 'merch') NOT NULL,
    image_url VARCHAR(500),
    stock INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ========================================
-- TABLE DES SKINS (produits virtuels)
-- ========================================
CREATE TABLE IF NOT EXISTS skins (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    game_name VARCHAR(100) NOT NULL,
    rarity ENUM('common', 'rare', 'epic', 'legendary') DEFAULT 'common',
    weapon_type VARCHAR(50),
    character_name VARCHAR(100),
    collection_name VARCHAR(100),
    visual_effects TEXT,
    release_date DATE,
    is_limited BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- ========================================
-- TABLE DES MERCH (produits physiques)
-- ========================================
CREATE TABLE IF NOT EXISTS merch (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    sizes VARCHAR(100),
    material VARCHAR(100),
    brand VARCHAR(100),
    color VARCHAR(50),
    official BOOLEAN DEFAULT FALSE,
    licensing_info TEXT,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- ========================================
-- TABLE DES CART ITEMS (PANIER)
-- ========================================
CREATE TABLE IF NOT EXISTS cart_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_product (user_id, product_id)
);

-- ========================================
-- TABLE DES COMMANDES
-- ========================================
CREATE TABLE IF NOT EXISTS orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status ENUM('pending', 'confirmed', 'shipped', 'delivered', 'cancelled') DEFAULT 'pending',
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    shipping_address TEXT,
    tracking_number VARCHAR(100),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ========================================
-- TABLE DES ORDER ITEMS (ARTICLES DE COMMANDE)
-- ========================================
CREATE TABLE IF NOT EXISTS order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    price_at_time DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- ========================================
-- TABLE DES HISTORIQUES D'ACHATS
-- ========================================
CREATE TABLE IF NOT EXISTS purchase_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    product_type ENUM('skin', 'merch') NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- ========================================
-- INDEX POUR OPTIMISATION
-- ========================================
CREATE INDEX idx_products_type ON products(type);
CREATE INDEX idx_products_user ON products(user_id);
CREATE INDEX idx_cart_items_user ON cart_items(user_id);
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_purchase_history_user ON purchase_history(user_id);
CREATE INDEX idx_purchase_history_date ON purchase_history(purchase_date);

-- ========================================
-- DONNÉES DE DÉMONSTRATION (OPTIONNEL)
-- ========================================

-- Utilisateur de démonstration
INSERT INTO users (username, email, password_hash, full_name) VALUES 
('admin', 'admin@pidev.com', 'hashed_password_demo', 'Administrateur PIDEV'),
('demo_user', 'demo@pidev.com', 'hashed_password_demo', 'Utilisateur Démonstration');

-- Produits de démonstration - SKINS
INSERT INTO products (name, description, price, type, image_url, stock, user_id) VALUES
('AK-47 Red Dragon', 'Skin légendaire AK-47 avec effets de feu et design draconique', 89.99, 'skin', 'https://example.com/ak47.jpg', 100, 1),
('AWP Asiimov', 'Skin rare AWP avec camouflage asiatique', 59.99, 'skin', 'https://example.com/awp.jpg', 50, 1),
('USP-S Kill Confirmed', 'Skin épique USP-S avec crâne confirmé', 74.99, 'skin', 'https://example.com/usp.jpg', 25, 1);

INSERT INTO skins (product_id, game_name, rarity, weapon_type, character_name, collection_name, visual_effects, release_date, is_limited) VALUES
(1, 'CS:GO', 'legendary', 'Assault Rifle', 'Unknown', 'Dragon Collection', 'Effets de feu animés, design rouge et noir', '2024-01-15', TRUE),
(2, 'CS:GO', 'rare', 'Sniper Rifle', 'Unknown', 'Asiimov Collection', 'Camouflage asiatique détaillé', '2024-02-01', FALSE),
(3, 'CS:GO', 'epic', 'Pistol', 'Unknown', 'Kill Confirmed', 'Design crâne avec effets sanglants', '2024-03-01', FALSE);

-- Produits de démonstration - MERCH
INSERT INTO products (name, description, price, type, image_url, stock, user_id) VALUES
('TSM Jersey 2024', 'Maillot officiel Team SoloMid 2024 avec logo brodé', 39.99, 'merch', 'https://example.com/tsm_jersey.jpg', 200, 1),
('Gaming Hoodie', 'Hoodie premium gaming avec logo discret et poche kangourou', 54.99, 'merch', 'https://example.com/hoodie.jpg', 150, 1),
('Pro Gaming Headset', 'Casque gaming professionnel avec micro et réduction de bruit', 79.99, 'merch', 'https://example.com/headset.jpg', 75, 1);

INSERT INTO merch (product_id, sizes, material, brand, color, official, licensing_info) VALUES
(5, 'S,M,L,XL,XXL', 'Coton organique', 'Team SoloMid', 'Noir/Bleu', TRUE, 'Licence officielle Team SoloMid 2024'),
(6, 'S,M,L,XL', 'Polyester microfibre', 'GamingPro', 'Noir', FALSE, 'Marque générique gaming'),
(7, 'Taille unique', 'Plastique/ABS', 'AudioTech', 'Noir/Rouge', FALSE, 'Compatible PC/Console');

-- ========================================
-- VUES POUR SIMPLIFIER LES REQUÊTES
-- ========================================

-- Vue des produits avec détails complets
CREATE VIEW product_details AS
SELECT 
    p.id,
    p.name,
    p.description,
    p.price,
    p.type,
    p.image_url,
    p.stock,
    p.created_at,
    p.updated_at,
    p.user_id,
    u.username as seller_name,
    u.full_name as seller_full_name,
    CASE 
        WHEN p.type = 'skin' THEN JSON_OBJECT(
            'game_name', s.game_name,
            'rarity', s.rarity,
            'weapon_type', s.weapon_type,
            'character_name', s.character_name,
            'collection_name', s.collection_name,
            'visual_effects', s.visual_effects,
            'release_date', s.release_date,
            'is_limited', s.is_limited
        )
        WHEN p.type = 'merch' THEN JSON_OBJECT(
            'sizes', m.sizes,
            'material', m.material,
            'brand', m.brand,
            'color', m.color,
            'official', m.official,
            'licensing_info', m.licensing_info
        )
    END as product_attributes
FROM products p
LEFT JOIN users u ON p.user_id = u.id
LEFT JOIN skins s ON p.id = s.product_id AND p.type = 'skin'
LEFT JOIN merch m ON p.id = m.product_id AND p.type = 'merch';

-- Vue du panier avec détails produits
CREATE VIEW cart_details AS
SELECT 
    ci.id,
    ci.user_id,
    ci.product_id,
    ci.quantity,
    ci.added_at,
    p.name as product_name,
    p.description as product_description,
    p.price as product_price,
    p.type as product_type,
    p.image_url,
    (ci.quantity * p.price) as total_price
FROM cart_items ci
JOIN products p ON ci.product_id = p.id;

-- Vue de l'historique d'achats
CREATE VIEW user_purchase_history AS
SELECT 
    ph.id,
    ph.user_id,
    ph.product_id,
    ph.product_name,
    ph.product_type,
    ph.price,
    ph.purchase_date,
    p.image_url,
    CASE 
        WHEN ph.product_type = 'skin' THEN (
            SELECT JSON_OBJECT(
                'game_name', s.game_name,
                'rarity', s.rarity,
                'weapon_type', s.weapon_type
            )
            FROM skins s WHERE s.product_id = ph.product_id
        )
        WHEN ph.product_type = 'merch' THEN (
            SELECT JSON_OBJECT(
                'sizes', m.sizes,
                'material', m.material,
                'brand', m.brand
            )
            FROM merch m WHERE m.product_id = ph.product_id
        )
    END as product_details
FROM purchase_history ph
JOIN products p ON ph.product_id = p.id;

-- ========================================
-- PROCÉDURES STOCKÉES (OPTIONNEL)
-- ========================================

DELIMITER //

-- Procédure pour ajouter un produit avec ses attributs spécifiques
CREATE PROCEDURE AddProductWithAttributes(
    IN p_name VARCHAR(255),
    IN p_description TEXT,
    IN p_price DECIMAL(10,2),
    IN p_type ENUM('skin', 'merch'),
    IN p_image_url VARCHAR(500),
    IN p_stock INT,
    IN p_user_id INT,
    -- Attributs SKIN
    IN s_game_name VARCHAR(100),
    IN s_rarity ENUM('common', 'rare', 'epic', 'legendary'),
    IN s_weapon_type VARCHAR(50),
    IN s_character_name VARCHAR(100),
    IN s_collection_name VARCHAR(100),
    IN s_visual_effects TEXT,
    IN s_release_date DATE,
    IN s_is_limited BOOLEAN,
    -- Attributs MERCH
    IN m_sizes VARCHAR(100),
    IN m_material VARCHAR(100),
    IN m_brand VARCHAR(100),
    IN m_color VARCHAR(50),
    IN m_official BOOLEAN,
    IN m_licensing_info TEXT
)
BEGIN
    -- Insérer le produit principal
    INSERT INTO products (name, description, price, type, image_url, stock, user_id)
    VALUES (p_name, p_description, p_price, p_type, p_image_url, p_stock, p_user_id);
    
    -- Récupérer l'ID du produit inséré
    SET @product_id = LAST_INSERT_ID();
    
    -- Insérer les attributs spécifiques selon le type
    IF p_type = 'skin' THEN
        INSERT INTO skins (product_id, game_name, rarity, weapon_type, character_name, collection_name, visual_effects, release_date, is_limited)
        VALUES (@product_id, s_game_name, s_rarity, s_weapon_type, s_character_name, s_collection_name, s_visual_effects, s_release_date, s_is_limited);
    ELSEIF p_type = 'merch' THEN
        INSERT INTO merch (product_id, sizes, material, brand, color, official, licensing_info)
        VALUES (@product_id, m_sizes, m_material, m_brand, m_color, m_official, m_licensing_info);
    END IF;
END //

DELIMITER ;

-- ========================================
-- DÉCLICHAGE DE FIN DE SCRIPT
-- ========================================

-- Afficher un résumé de la base de données créée
SELECT 'Database PIDEV Java created successfully!' as status;
SELECT 'Tables created: users, products, skins, merch, cart_items, orders, order_items, purchase_history' as tables_created;
SELECT 'Views created: product_details, cart_details, user_purchase_history' as views_created;
SELECT 'Stored procedures created: AddProductWithAttributes' as procedures_created;
