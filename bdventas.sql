-- Creacion  de esquema general para base de datos
CREATE SCHEMA catalogos;

-- Creacion de tabla de marcas
CREATE TABLE catalogos.marcas (
    id_marca SERIAL PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL UNIQUE,
    pais_origen VARCHAR(80) NOT NULL,
    estado SMALLINT NOT NULL DEFAULT 1,

    CONSTRAINT chk_marca_estado
        CHECK (estado IN (0, 1))
);

/* Pruebas para validar los CONSTRAINS en catalogos.marcas

-- Prueba 1 no permite valores nulos
INSERT INTO catalogos.marcas(nombre,pais_origen,estado) VALUES (NULL, 'Nicaragua', 1);
-- Prueba 2 permite si los datos estan bien mas no que los datos sean repetidos
INSERT INTO catalogos.marcas(nombre,pais_origen,estado) VALUES ('Almohada', 'Nicaragua', 1);
-- prueba 3 Agregar el registro por defecto de una fila en la columna estado
INSERT INTO catalogos.marcas(nombre,pais_origen) VALUES ('Audifonos', 'Nicaragua');
 */

/*
    Nota: Luego de hacer varias pruebas, por alguna razon, si dejaba las pruebas como codigo, y no como comentario
    luego al ejecutar mis INSERT en las tablas, al llegar a la tabla producto me daba un error de
    "violates foreign key constraint. Parece de que, aunque una consulta no genere registro,
    por ejemplo la prueba 1 donde hay un valor NULL que viola una CONSTRAIN,
    parece de que Postgre guarda ese INSERT malo como un id.
*/

-- Bueno la solucion era poner los CREATE TABLE, luego los INSERT validos, y luego hacer las pruebas de CONSTRAINS
-- Pero, pues decidi seguir la estructura que usted uso, primero la tabla, despues probar, y de ultimo meter los datos
-- Solo que yo si tuve que dejar como comentarios las pruebas, por la explicacion que le di anteriormente

-- Creacion de tabla de categorias
CREATE TABLE catalogos.categorias (
    id_categorias SERIAL PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    estado SMALLINT NOT NULL DEFAULT 1,

    CONSTRAINT chk_categoria_estado
        CHECK (estado IN (0, 1))
);

/* Pruebas para validar los CONSTRAINTS en catalogos.categorias

-- Prueba 1 no permite valores nulos
INSERT INTO catalogos.categorias(nombre,descripcion, estado) VALUES (NULL, 'productos de belleza', 1);
-- prueba 2 se me ocurrio un registro con un estado fuera del rango 0 , 1.
INSERT INTO catalogos.categorias(nombre,descripcion) VALUES ('deporte', 'accesorios de diversos deportes', 3);
*/

-- Creacion de tabla de productos
CREATE TABLE catalogos.productos (
    id_productos SERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL UNIQUE,
    descripcion VARCHAR(120),
    precio NUMERIC(10,2) NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    fecha_creacion DATE NOT NULL DEFAULT CURRENT_DATE,
    id_categoria INTEGER NOT NULL,
    id_marca INTEGER NOT NULL,
    estado SMALLINT NOT NULL DEFAULT 1,

    CONSTRAINT fk_producto_categoria
        FOREIGN KEY (id_categoria)
        REFERENCES catalogos.categorias(id_categorias),

    CONSTRAINT fk_producto_marca
        FOREIGN KEY (id_marca)
        REFERENCES catalogos.marcas(id_marca),

        CONSTRAINT chk_producto_precio
            CHECK (precio > 0),

        CONSTRAINT chk_producto_stock
            CHECK (stock >= 0),

        CONSTRAINT chk_producto_estado
            CHECK (estado IN (0, 1))
);

-- Insertar 5 Marcas
INSERT INTO catalogos.marcas (nombre, pais_origen) VALUES
                                                       ('Nike', 'Estados Unidos'),
                                                       ('Lenovo', 'China'),
                                                       ('Samsung', 'Corea del Sur'),
                                                       ('Sony', 'Japón'),
                                                       ('Adidas', 'Alemania');

-- Insertar 5 categorias
INSERT INTO catalogos.categorias (nombre, descripcion) VALUES
                                                           ('Electrónica', 'Dispositivos electrónicos y componentes'),
                                                           ('Computación', 'Laptops y periféricos'),
                                                           ('Telefonía', NULL),
                                                           ('Ropa y Calzado', 'Prendas y calzado deportivo'),
                                                           ('Audio y Video', NULL);

--  Insertar 15 productos
INSERT INTO catalogos.productos (codigo, nombre, descripcion, precio, stock, id_categoria, id_marca) VALUES
                                                                                                         ('PRD-LEN-001', 'Laptop Lenovo IdeaPad 3', 'Portátil 15.6 pulgadas Ryzen 5', 549.99, 15, 2, 2),
                                                                                                         ('PRD-LEN-002', 'Laptop Lenovo Legion 5 Pro', 'Laptop gamer RTX 4060', 1299.50, 8, 2, 2),
                                                                                                         ('PRD-SAM-001', 'Samsung Galaxy S24 Ultra', 'Smartphone 256GB Titanium Gray', 1199.99, 12, 3, 3),
                                                                                                         ('PRD-SAM-002', 'Samsung Galaxy A55 5G', 'Smartphone 128GB pantalla Super AMOLED', 389.00, 25, 3, 3),
                                                                                                         ('PRD-SAM-003', 'Monitor Samsung Odyssey G5 27"', 'Monitor curvo gaming 144Hz', 279.99, 10, 2, 3),
                                                                                                         ('PRD-SAM-004', 'Smart TV Samsung 55" QLED 4K', 'Televisor HDR10+', 649.00, 7, 1, 3),
                                                                                                         ('PRD-SNY-001', 'Audífonos Sony WH-1000XM5', 'Cancelación de ruido premium', 348.00, 18, 5, 4),
                                                                                                         ('PRD-SNY-002', 'Parlante Bluetooth Sony SRS-XB13', 'Parlante portátil IP67', 49.99, 30, 5, 4),
                                                                                                         ('PRD-SNY-003', 'Barra de Sonido Sony HT-S400', 'Sistema 2.1 canales', 199.99, 9, 5, 4),
                                                                                                         ('PRD-NKE-001', 'Zapatillas Nike Air Force 1 07', 'Calzado deportivo clásico', 115.00, 40, 4, 1),
                                                                                                         ('PRD-NKE-002', 'Camiseta Nike Dri-FIT Park VII', 'Camiseta transpirable', 24.50, 50, 4, 1),
                                                                                                         ('PRD-NKE-003', 'Sudadera Nike Club Fleece', 'Buzo con capucha afelpado', 65.00, 20, 4, 1),
                                                                                                         ('PRD-ADI-001', 'Zapatillas Adidas Ultraboost Light', 'Calzado running Boost', 180.00, 22, 4, 5),
                                                                                                         ('PRD-ADI-002', 'Pantalón Adidas Tiro 23 League', 'Pantalón de chándal AEROREADY', 45.00, 35, 4, 5),
                                                                                                         ('PRD-LEN-003', 'Tablet Lenovo Tab M11', NULL, 159.00, 0, 2, 2);

/* Pruebas para validar los CONSTRAINTS en catalogos.productos

-- Falla: Restricción NOT NULL en 'precio'
INSERT INTO catalogos.productos (codigo, nombre, descripcion, precio, stock, id_categoria, id_marca)
VALUES ('PRD-ERR-007', 'Sin Precio', 'Prueba NOT NULL en precio', NULL, 10, 2, 4);

-- Falla: Restricción NOT NULL en llave foránea 'id_categoria'
INSERT INTO catalogos.productos (codigo, nombre, descripcion, precio, stock, id_categoria, id_marca)
VALUES ('PRD-ERR-008', 'Sin Categoria', 'Prueba NOT NULL en id_categoria', 80.00, 10, NULL, 4);

-- Falla: Restricción NOT NULL en llave foránea 'id_marca'
INSERT INTO catalogos.productos (codigo, nombre, descripcion, precio, stock, id_categoria, id_marca)
VALUES ('PRD-ERR-010', 'Sin Marca', 'Prueba NOT NULL en id_marca', 99.99, 10, 2, NULL);

-- Falla: Restricción NOT NULL en 'codigo'
INSERT INTO catalogos.productos (codigo, nombre, descripcion, precio, stock, id_categoria, id_marca)
VALUES (NULL, 'Producto Sin Codigo', 'Prueba NOT NULL en codigo', 99.99, 10, 2, 2);

-- Falla: Longitud excedida en 'codigo' (máximo VARCHAR(20), se envían 25 caracteres)
INSERT INTO catalogos.productos (codigo, nombre, descripcion, precio, stock, id_categoria, id_marca)
VALUES ('PRD-CODIGO-DEMASIADO-LARGO-01', 'Codigo Largo', 'Prueba VARCHAR(20)', 99.99, 10, 2, 2);

-- Falla: Desbordamiento en 'precio' NUMERIC(10,2) (excede la cantidad de digitos permitida, que son 10, de los cuals tambien cuentan los decimales)
INSERT INTO catalogos.productos (codigo, nombre, descripcion, precio, stock, id_categoria, id_marca)
VALUES ('PRD-ERR-011', 'Precio Fuera de Rango', 'Prueba NUMERIC(10,2)', 100000000.00, 10, 2, 2);

-- Falla: UNIQUE en 'codigo' (el código ya fue registrado)
INSERT INTO catalogos.productos (codigo, nombre, descripcion, precio, stock, id_categoria, id_marca)
VALUES ('PRD-LEN-001', 'Laptop Lenovo Alternativa', 'Prueba UNIQUE codigo', 499.99, 10, 2, 2);

-- Falla: UNIQUE en 'nombre' (el nombre ya fue registrado)
INSERT INTO catalogos.productos (codigo, nombre, descripcion, precio, stock, id_categoria, id_marca)
VALUES ('PRD-NEW-999', 'Laptop Lenovo IdeaPad 3', 'Prueba UNIQUE nombre', 499.99, 10, 2, 2);

-- Falla: fk_producto_categoria (la categoría 99 no existe)
INSERT INTO catalogos.productos (codigo, nombre, descripcion, precio, stock, id_categoria, id_marca)
VALUES ('PRD-ERR-001', 'Producto Categoria Invalida', 'Prueba FK categoria', 50.00, 10, 99, 2);

-- Falla: fk_producto_marca (la marca 99 no existe)
INSERT INTO catalogos.productos (codigo, nombre, descripcion, precio, stock, id_categoria, id_marca)
VALUES ('PRD-ERR-002', 'Producto Marca Invalida', 'Prueba FK marca', 50.00, 10, 2, 99);

-- Falla: chk_producto_precio (el precio debe ser mayor a 0; probando con 0)
INSERT INTO catalogos.productos (codigo, nombre, descripcion, precio, stock, id_categoria, id_marca)
VALUES ('PRD-ERR-003', 'Producto Precio Invalido', 'Prueba CHECK precio', 0.00, 10, 2, 2);

-- Falla: chk_producto_stock (el stock debe ser mayor o igual a 0; probando con negativo)
INSERT INTO catalogos.productos (codigo, nombre, descripcion, precio, stock, id_categoria, id_marca)
VALUES ('PRD-ERR-004', 'Producto Stock Invalido', 'Prueba CHECK stock', 15.00, -5, 2, 2);

-- Falla: chk_producto_estado (el estado debe ser 0 o 1; probando con 2)
INSERT INTO catalogos.productos (codigo, nombre, descripcion, precio, stock, id_categoria, id_marca, estado)
VALUES ('PRD-ERR-005', 'Producto Estado Invalido', 'Prueba CHECK estado', 15.00, 10, 2, 2, 2);
 */

-- Consulta 1:
SELECT *
FROM catalogos.productos
WHERE estado = 1 AND stock >=0;

-- Consulta 2:
SELECT *
FROM catalogos.productos
WHERE precio >= 50;

-- Consulta 3:
SELECT *
FROM catalogos.productos
WHERE precio BETWEEN 20 AND 100;

SELECT *
FROM catalogos.productos
WHERE precio >= 20 AND precio <=100;

-- Consulta 4:
SELECT *
FROM catalogos.productos
WHERE nombre ILIKE '%laptop%';

-- Consulta 5:
SELECT *
FROM catalogos.productos
WHERE codigo ILIKE '%PRD-SAM%' OR nombre ILIKE '%samsung%';

SELECT
    p.codigo,
    p.nombre ,
    c.nombre  AS Categoria,
    m.nombre AS Marca,
    p.precio,
    p.stock,
    p.fecha_creacion
FROM catalogos.productos AS p
         INNER JOIN catalogos.categorias AS c ON p.id_categoria = c.id_categorias
         INNER JOIN catalogos.marcas AS m ON p.id_marca = m.id_marca
WHERE p.estado=1;