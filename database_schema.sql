-- =============================================
-- Base de données pour l'application de Blog
-- =============================================

-- Création de la base de données
CREATE DATABASE IF NOT EXISTS blog_app CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE blog_app;

-- =============================================
-- Table des utilisateurs
-- =============================================
CREATE TABLE IF NOT EXISTS utilisateurs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    role ENUM('ADMIN', 'USER') DEFAULT 'USER',
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- Table des posts
-- =============================================
CREATE TABLE IF NOT EXISTS posts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    contenu TEXT NOT NULL,
    image_url VARCHAR(500),
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id INT NOT NULL,
    likes INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    INDEX idx_date_creation (date_creation),
    INDEX idx_user_id (user_id),
    INDEX idx_likes (likes)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- Table des commentaires
-- =============================================
CREATE TABLE IF NOT EXISTS commentaires (
    id INT AUTO_INCREMENT PRIMARY KEY,
    contenu TEXT NOT NULL,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    post_id INT NOT NULL,
    user_id INT NOT NULL,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    INDEX idx_post_id (post_id),
    INDEX idx_date_creation (date_creation)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- Table des likes (pour suivre qui a liké quoi)
-- =============================================
CREATE TABLE IF NOT EXISTS post_likes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL,
    user_id INT NOT NULL,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    UNIQUE KEY unique_like (post_id, user_id),
    INDEX idx_post_user (post_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- Table des notifications email
-- =============================================
CREATE TABLE IF NOT EXISTS notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL, -- 'LIKE', 'COMMENT', 'MENTION'
    message TEXT NOT NULL,
    email_destinataire VARCHAR(100) NOT NULL,
    statut ENUM('ENVOYE', 'ERREUR', 'EN_ATTENTE') DEFAULT 'EN_ATTENTE',
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    post_id INT,
    user_id INT,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES utilisateurs(id) ON DELETE SET NULL,
    INDEX idx_email_destinataire (email_destinataire),
    INDEX idx_statut (statut),
    INDEX idx_date_creation (date_creation)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- Table des traductions (cache pour optimiser)
-- =============================================
CREATE TABLE IF NOT EXISTS traductions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    texte_original TEXT NOT NULL,
    langue_source VARCHAR(10) NOT NULL,
    langue_cible VARCHAR(10) NOT NULL,
    texte_traduit TEXT NOT NULL,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_traduction (texte_original(255), langue_source, langue_cible),
    INDEX idx_texte_original (texte_original(255)),
    INDEX idx_langue_source (langue_source),
    INDEX idx_langue_cible (langue_cible)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- Insertion de données de test
-- =============================================

-- Utilisateurs de test
INSERT INTO utilisateurs (nom, email, mot_de_passe, role) VALUES
('Admin', 'admin@blog.com', '$2a$10$K8Q8pK4v8Q8Q8Q8Q8Q8O8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q', 'ADMIN'),
('Jean Dupont', 'jean@email.com', '$2a$10$K8Q8pK4v8Q8Q8Q8Q8O8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q', 'USER'),
('Mohamed Ali', 'mohamed@email.com', '$2a$10$K8Q8pK4v8Q8Q8Q8Q8O8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q', 'USER');

-- Posts de test
INSERT INTO posts (titre, contenu, image_url, user_id, likes) VALUES
('Bienvenue sur notre blog !', 'Ceci est le premier article de notre blog. Nous sommes ravis de vous accueillir et espérons que vous apprécierez le contenu que nous partageons.', 'https://res.cloudinary.com/imageboss/image/upload/v1/blog_posts/welcome.jpg', 1, 15),
('Les avantages de JavaFX', 'JavaFX est une plateforme moderne pour créer des applications desktop avec Java. Elle offre des composants UI riches, des animations fluides et une excellente intégration avec Maven.', 'https://res.cloudinary.com/imageboss/image/upload/v1/blog_posts/javafx.jpg', 2, 23),
('Tutoriel MySQL', 'MySQL est un système de gestion de bases de données relationnelles très populaire. Dans ce tutoriel, nous allons voir comment créer des tables et effectuer des requêtes SQL de base.', 'https://res.cloudinary.com/imageboss/image/upload/v1/blog_posts/mysql.jpg', 3, 18),
('Introduction à Cloudinary', 'Cloudinary est un service de gestion d\'images dans le cloud qui offre des fonctionnalités puissantes : upload, transformation, optimisation et livraison via CDN.', 'https://res.cloudinary.com/imageboss/image/upload/v1/blog_posts/cloudinary.jpg', 1, 31),
('Les meilleures pratiques en développement web', 'Le développement web moderne nécessite de suivre certaines meilleures pratiques : code propre, tests unitaires, intégration continue et déploiement automatisé.', 'https://res.cloudinary.com/imageboss/image/upload/v1/blog_posts/webdev.jpg', 2, 27);

-- Commentaires de test
INSERT INTO commentaires (contenu, post_id, user_id) VALUES
('Excellent article ! Très bien expliqué.', 1, 2),
('Merci pour ce tutoriel, ça m\'a beaucoup aidé.', 2, 3),
('Pouvez-vous faire un article sur Spring Boot ?', 3, 2),
('Super contenu, j\'attends la suite avec impatience !', 1, 3),
('Les exemples sont très clairs, merci !', 4, 2);

-- Likes de test (pour la table post_likes)
INSERT INTO post_likes (post_id, user_id) VALUES
(1, 2), (1, 3),
(2, 1), (2, 3),
(3, 1), (3, 2),
(4, 1), (4, 2), (4, 3),
(5, 2), (5, 3);

-- =============================================
-- Procédures stockées utiles
-- =============================================

-- Procédure pour ajouter un like
DELIMITER //
CREATE PROCEDURE ajouter_like(IN p_post_id INT, IN p_user_id INT)
BEGIN
    DECLARE v_like_count INT;
    
    -- Vérifier si l'utilisateur a déjà liké
    IF NOT EXISTS (SELECT 1 FROM post_likes WHERE post_id = p_post_id AND user_id = p_user_id) THEN
        -- Ajouter le like
        INSERT INTO post_likes (post_id, user_id) VALUES (p_post_id, p_user_id);
        
        -- Mettre à jour le compteur de likes dans posts
        UPDATE posts SET likes = likes + 1 WHERE id = p_post_id;
        
        -- Récupérer le nouveau nombre de likes
        SELECT likes INTO v_like_count FROM posts WHERE id = p_post_id;
        
        SELECT v_like_count AS nouveau_nombre_likes;
    ELSE
        SELECT -1 AS erreur; -- L'utilisateur a déjà liké
    END IF;
END //
DELIMITER ;

-- Procédure pour supprimer un like
DELIMITER //
CREATE PROCEDURE supprimer_like(IN p_post_id INT, IN p_user_id INT)
BEGIN
    DECLARE v_like_count INT;
    
    -- Vérifier si l'utilisateur a liké
    IF EXISTS (SELECT 1 FROM post_likes WHERE post_id = p_post_id AND user_id = p_user_id) THEN
        -- Supprimer le like
        DELETE FROM post_likes WHERE post_id = p_post_id AND user_id = p_user_id;
        
        -- Mettre à jour le compteur de likes dans posts
        UPDATE posts SET likes = likes - 1 WHERE id = p_post_id AND likes > 0;
        
        -- Récupérer le nouveau nombre de likes
        SELECT likes INTO v_like_count FROM posts WHERE id = p_post_id;
        
        SELECT v_like_count AS nouveau_nombre_likes;
    ELSE
        SELECT -1 AS erreur; -- L'utilisateur n'avait pas liké
    END IF;
END //
DELIMITER ;

-- =============================================
-- Vues pour les requêtes complexes
-- =============================================

-- Vue des posts avec informations utilisateur
CREATE OR REPLACE VIEW v_posts_details AS
SELECT 
    p.id,
    p.titre,
    p.contenu,
    p.image_url,
    p.date_creation,
    p.likes,
    u.nom AS auteur_nom,
    u.email AS auteur_email,
    COUNT(c.id) AS nombre_commentaires
FROM posts p
LEFT JOIN utilisateurs u ON p.user_id = u.id
LEFT JOIN commentaires c ON p.id = c.post_id
GROUP BY p.id, u.id;

-- Vue des posts populaires
CREATE OR REPLACE VIEW v_posts_populaires AS
SELECT 
    p.*,
    u.nom AS auteur_nom,
    COUNT(c.id) AS nombre_commentaires
FROM posts p
JOIN utilisateurs u ON p.user_id = u.id
LEFT JOIN commentaires c ON p.id = c.post_id
WHERE p.likes > 10
GROUP BY p.id, u.id
ORDER BY p.likes DESC;

-- Vue des activités récentes
CREATE OR REPLACE VIEW v_activites_recentes AS
SELECT 
    'POST' AS type_activite,
    p.titre AS description,
    p.date_creation,
    u.nom AS utilisateur
FROM posts p
JOIN utilisateurs u ON p.user_id = u.id

UNION ALL

SELECT 
    'COMMENTAIRE' AS type_activite,
    CONCAT('Commentaire sur: ', p.titre) AS description,
    c.date_creation,
    u.nom AS utilisateur
FROM commentaires c
JOIN posts p ON c.post_id = p.id
JOIN utilisateurs u ON c.user_id = u.id

ORDER BY date_creation DESC
LIMIT 50;

-- =============================================
-- Index pour optimiser les performances
-- =============================================

-- Index composite pour les recherches full-text
CREATE FULLTEXT INDEX ft_posts_titre_contenu ON posts(titre, contenu);

-- Index pour la pagination
CREATE INDEX idx_posts_pagination ON posts(date_creation DESC, id);

-- Index pour les statistiques
CREATE INDEX idx_posts_stats ON posts(user_id, date_creation, likes);

-- =============================================
-- Triggers pour maintenir la cohérence
-- =============================================

-- Trigger pour mettre à jour le compteur de likes
DELIMITER //
CREATE TRIGGER after_insert_like
AFTER INSERT ON post_likes
FOR EACH ROW
BEGIN
    UPDATE posts SET likes = likes + 1 WHERE id = NEW.post_id;
END//
DELIMITER ;

DELIMITER //
CREATE TRIGGER after_delete_like
AFTER DELETE ON post_likes
FOR EACH ROW
BEGIN
    UPDATE posts SET likes = likes - 1 WHERE id = OLD.post_id AND likes > 0;
END//
DELIMITER ;

-- Trigger pour enregistrer les notifications
DELIMITER //
CREATE TRIGGER after_insert_commentaire
AFTER INSERT ON commentaires
FOR EACH ROW
BEGIN
    DECLARE auteur_email VARCHAR(100);
    
    -- Récupérer l'email de l'auteur du post
    SELECT u.email INTO auteur_email
    FROM posts p
    JOIN utilisateurs u ON p.user_id = u.id
    WHERE p.id = NEW.post_id;
    
    -- Insérer une notification
    INSERT INTO notifications (type, message, email_destinataire, post_id, user_id)
    VALUES (
        'COMMENT',
        CONCAT('Nouveau commentaire sur votre post: ', (SELECT titre FROM posts WHERE id = NEW.post_id)),
        auteur_email,
        NEW.post_id,
        NEW.user_id
    );
END//
DELIMITER ;
