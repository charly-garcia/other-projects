-- Flyway Migration V1: Initial schema for Matriz de Usuarios
-- Creates all six tables with proper constraints and foreign keys

-- Table: roles
-- Stores role catalog for applications
CREATE TABLE roles (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_roles_name UNIQUE (name)
);

-- Table: areas
-- Stores organizational areas catalog
CREATE TABLE areas (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_areas_name UNIQUE (name)
);

-- Table: companies
-- Stores companies catalog
CREATE TABLE companies (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    country     VARCHAR(100) NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_companies_name UNIQUE (name)
);

-- Table: suppliers
-- Stores suppliers catalog with compliance indicator
CREATE TABLE suppliers (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    compliance  TINYINT(1) NOT NULL DEFAULT 0,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_suppliers_name UNIQUE (name)
);

-- Table: applications
-- Stores corporate applications inventory
-- Each application is associated with a role
CREATE TABLE applications (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    owner       VARCHAR(100) NOT NULL,
    url         VARCHAR(500) NOT NULL,
    role_id     BIGINT NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_applications_name UNIQUE (name),
    CONSTRAINT fk_applications_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Table: users
-- Stores user information with references to catalogs
-- Includes ENUM columns for user_type, status, scope, and information_access
CREATE TABLE users (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    name               VARCHAR(150) NOT NULL,
    email              VARCHAR(255) NOT NULL,
    user_type          ENUM('Interno','Practicante','Contractor') NOT NULL,
    status             ENUM('ACTIVO','INACTIVO') NOT NULL,
    start_date         DATE NOT NULL,
    scope              ENUM('PCI','ISO','General') NOT NULL,
    information_access ENUM('Secreta','Confidencial','Uso Interno') NOT NULL,
    area_id            BIGINT,
    company_id         BIGINT,
    supplier_id        BIGINT,
    application_id     BIGINT,
    role_id            BIGINT,
    position           VARCHAR(150),
    manager            VARCHAR(150),
    end_date           DATE,
    created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_users_email      UNIQUE (email),
    CONSTRAINT fk_users_area       FOREIGN KEY (area_id)        REFERENCES areas(id),
    CONSTRAINT fk_users_company    FOREIGN KEY (company_id)     REFERENCES companies(id),
    CONSTRAINT fk_users_supplier   FOREIGN KEY (supplier_id)    REFERENCES suppliers(id),
    CONSTRAINT fk_users_application FOREIGN KEY (application_id) REFERENCES applications(id),
    CONSTRAINT fk_users_role       FOREIGN KEY (role_id)        REFERENCES roles(id)
);
