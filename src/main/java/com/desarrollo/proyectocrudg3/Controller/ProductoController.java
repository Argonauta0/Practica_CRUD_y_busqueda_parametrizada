package com.desarrollo.proyectocrudg3.Controller;

import com.desarrollo.proyectocrudg3.Database.DatabaseConnection;
import com.desarrollo.proyectocrudg3.Model.Categoria;
import com.desarrollo.proyectocrudg3.Model.Marca;
import com.desarrollo.proyectocrudg3.Model.Producto;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductoController {

    @FXML private TextField txtCodigo;
    @FXML private TextField txtProducto;
    @FXML private TextField txtDescripcion;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtStock;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private ComboBox<Marca> cmbMarca;

    @FXML private Button btnNuevo;
    @FXML private Button btnGuardar;
    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarFiltros;

    @FXML private TableView<Producto> tblProducto;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, String> colMarca;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, Date> colFecha;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<Categoria> cmbCategoriaVista2;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private TextField txtPrecioMIN;
    @FXML private TextField txtPrecioMAX;
    @FXML private CheckBox chkExiste;
    @FXML private ComboBox<String> cmbOrdenar;

    private final ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    private final ObservableList<Categoria> listaCategorias = FXCollections.observableArrayList();
    private final ObservableList<Marca> listaMarcas = FXCollections.observableArrayList();

    private Connection conectar() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    @FXML
    public void initialize() {
        configurarTableView();
        configurarSeleccionTableView();
        cargarCategorias();
        cargarMarcas();
        cargarOpcionesOrden();
        dpFecha.setValue(LocalDate.now());
        cargarProductos();
    }

    private void configurarTableView() {
        colCodigo.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCodigo()));
        colNombre.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNombre()));
        colCategoria.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCategoria()));
        colMarca.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getMarca()));
        colPrecio.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getPrecio()).asObject());
        colStock.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getStock()).asObject());
        colFecha.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().getFechaCreacion()));

        tblProducto.setItems(listaProductos);
    }

    private void configurarSeleccionTableView() {
        tblProducto.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        cargarProductoEnFormulario(newValue);
                    }
                });
    }

    private void cargarProductoEnFormulario(Producto p) {
        txtCodigo.setText(p.getCodigo());
        txtProducto.setText(p.getNombre());
        txtDescripcion.setText(p.getDescripcion());
        txtPrecio.setText(String.valueOf(p.getPrecio()));
        txtStock.setText(String.valueOf(p.getStock()));
        dpFecha.setValue(p.getFechaCreacion() != null ? p.getFechaCreacion().toLocalDate() : null);

        for (Categoria categoria : listaCategorias) {
            if (categoria.getIdCategorias() == p.getIdCategoria()) {
                cmbCategoria.setValue(categoria);
                break;
            }
        }

        for (Marca marca : listaMarcas) {
            if (marca.getIdMarca() == p.getIdMarca()) {
                cmbMarca.setValue(marca);
                break;
            }
        }
    }

    private void cargarCategorias() {
        listaCategorias.clear();

        String sql = """
                SELECT
                    id_categorias,
                    nombre
                FROM catalogos.categorias
                WHERE estado = 1
                ORDER BY nombre
                """;

        try (
                Connection conexion = conectar();
                PreparedStatement ps = conexion.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Categoria categoria = new Categoria(
                        rs.getInt("id_categorias"),
                        rs.getString("nombre"),
                        null,
                        1
                );
                listaCategorias.add(categoria);
            }

            cmbCategoria.setItems(FXCollections.observableArrayList(listaCategorias));
            cmbCategoriaVista2.setItems(FXCollections.observableArrayList(listaCategorias));

        } catch (SQLException e) {
            mostrarErrorBaseDatos("No se pudieron cargar las categorías.", e);
        }
    }

    private void cargarMarcas() {
        listaMarcas.clear();

        String sql = """
                SELECT
                    id_marca,
                    nombre
                FROM catalogos.marcas
                WHERE estado = 1
                ORDER BY nombre
                """;

        try (
                Connection conexion = conectar();
                PreparedStatement ps = conexion.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Marca marca = new Marca(
                        rs.getInt("id_marca"),
                        rs.getString("nombre"),
                        null,
                        1
                );
                listaMarcas.add(marca);
            }

            cmbMarca.setItems(FXCollections.observableArrayList(listaMarcas));

        } catch (SQLException e) {
            mostrarErrorBaseDatos("No se pudieron cargar las marcas.", e);
        }
    }

    private void cargarOpcionesOrden() {
        cmbOrdenar.setItems(
                FXCollections.observableArrayList(
                        "Nombre",
                        "Precio menor a mayor",
                        "Precio mayor a menor",
                        "Fecha más reciente",
                        "Fecha más antigua",
                        "Stock mayor a menor",
                        "Stock menor a mayor"
                )
        );
        cmbOrdenar.setValue("Nombre");
    }

    private Producto crearProductoDesdeResultSet(ResultSet rs) throws SQLException {
        Producto producto = new Producto(
                rs.getInt("id_productos"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getDouble("precio"),
                rs.getInt("stock"),
                rs.getDate("fecha_creacion"),
                rs.getInt("id_categoria"),
                rs.getInt("id_marca"),
                rs.getInt("estado")
        );
        producto.setCategoria(rs.getString("categoria"));
        producto.setMarca(rs.getString("marca"));
        return producto;
    }

    @FXML
    private void agregarProducto() {
        limpiarFormulario();
    }

    @FXML // Enlaza este método con el botón Guardar del FXML (onAction="#guardarProducto")
    private void guardarProducto() { // Método que inserta un producto nuevo en la base de datos
        if (!validarFormulario()) { // Si el formulario tiene datos vacíos o inválidos...
            return; // ...sale del método sin guardar nada
        } // Fin de la validación del formulario

        String codigo = txtCodigo.getText().trim(); // Lee el código escrito y le quita espacios al inicio y al final

        if (existeCodigo(codigo, null)) { // Consulta en la BD si el código ya existe (null = no excluye ningún producto)
            mostrarAlerta( // Muestra un cuadro de aviso al usuario
                    Alert.AlertType.WARNING, // Tipo de alerta: advertencia
                    "Código duplicado", // Título de la alerta
                    "El código " + codigo + " ya pertenece a otro producto." // Mensaje que incluye el código repetido
            ); // Cierra la llamada a mostrarAlerta
            txtCodigo.requestFocus(); // Coloca el cursor en el campo código para corregirlo
            return; // Sale del método sin insertar
        } // Fin de la verificación de código duplicado

        String sql = """
                INSERT INTO catalogos.productos -- Inserta un registro en la tabla productos del esquema catalogos
                (                               -- Abre la lista de columnas que se van a llenar
                    codigo,                     -- Columna 1: código único del producto
                    nombre,                     -- Columna 2: nombre del producto
                    descripcion,                -- Columna 3: descripción (puede ser NULL)
                    precio,                     -- Columna 4: precio de venta
                    stock,                      -- Columna 5: cantidad disponible
                    fecha_creacion,             -- Columna 6: fecha de registro
                    id_categoria,               -- Columna 7: llave foránea hacia categorias
                    id_marca,                   -- Columna 8: llave foránea hacia marcas
                    estado                      -- Columna 9: estado del registro
                )                               -- Cierra la lista de columnas
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1) -- 8 parámetros que se llenan desde Java y estado fijo en 1 (activo)
                """; // Fin del texto SQL guardado en la variable sql

        try ( // try-with-resources: cierra la conexión y el statement automáticamente al terminar
              Connection conexion = conectar(); // Abre la conexión con PostgreSQL
              PreparedStatement ps = conexion.prepareStatement(sql) // Prepara la consulta con parámetros (evita inyección SQL)
        ) { // Inicio del bloque try
            ps.setString(1, codigo); // Asigna el código al primer parámetro
            ps.setString(2, txtProducto.getText().trim()); // Asigna el nombre al segundo parámetro
            ps.setString(3, obtenerDescripcion()); // Asigna la descripción, o NULL si quedó vacía
            ps.setBigDecimal(4, new BigDecimal(txtPrecio.getText().trim())); // Convierte el precio a BigDecimal y lo asigna
            ps.setInt(5, Integer.parseInt(txtStock.getText().trim())); // Convierte el stock a entero y lo asigna
            ps.setDate(6, Date.valueOf(dpFecha.getValue())); // Convierte el LocalDate del DatePicker a java.sql.Date
            ps.setInt(7, cmbCategoria.getValue().getIdCategorias()); // Asigna el ID de la categoría seleccionada
            ps.setInt(8, cmbMarca.getValue().getIdMarca()); // Asigna el ID de la marca seleccionada

            int filasAfectadas = ps.executeUpdate(); // Ejecuta el INSERT y devuelve cuántas filas se insertaron

            if (filasAfectadas > 0) { // Si se insertó al menos una fila...
                mostrarAlerta( // ...informa al usuario
                        Alert.AlertType.INFORMATION, // Tipo de alerta: información
                        "Registro guardado", // Título de la alerta
                        "El producto fue registrado correctamente." // Mensaje de éxito
                ); // Cierra la llamada a mostrarAlerta
                limpiarFormulario(); // Deja el formulario vacío para un nuevo registro
                cargarProductos(); // Recarga la tabla para que aparezca el producto nuevo
            } // Fin del if de filas afectadas
        } catch (SQLException e) { // Captura cualquier error devuelto por PostgreSQL
            manejarErrorRestriccion(e, "No se pudo guardar el producto."); // Traduce el error (duplicado, CHECK, longitud) a un mensaje claro
        } // Fin del catch
    } // Fin del método guardarProducto

    private void cargarProductos() { // Método que consulta todos los productos activos y los muestra en la tabla
        listaProductos.clear(); // Vacía la lista para no duplicar productos en la tabla

        String sql = """
                SELECT                                  -- Inicia la consulta de lectura
                    p.id_productos,                     -- ID del producto
                    p.codigo,                           -- Código del producto
                    p.nombre,                           -- Nombre del producto
                    p.descripcion,                      -- Descripción del producto
                    p.precio,                           -- Precio del producto
                    p.stock,                            -- Stock disponible
                    p.fecha_creacion,                   -- Fecha de creación
                    p.id_categoria,                     -- ID de la categoría (para el ComboBox)
                    c.nombre AS categoria,              -- Nombre de la categoría con alias categoria
                    p.id_marca,                         -- ID de la marca (para el ComboBox)
                    m.nombre AS marca,                  -- Nombre de la marca con alias marca
                    p.estado                            -- Estado del producto
                FROM catalogos.productos p              -- Tabla principal con alias p
                INNER JOIN catalogos.categorias c       -- Une con la tabla categorias usando el alias c
                    ON p.id_categoria = c.id_categorias -- Condición de unión por el ID de categoría
                INNER JOIN catalogos.marcas m           -- Une con la tabla marcas usando el alias m
                    ON p.id_marca = m.id_marca          -- Condición de unión por el ID de marca
                WHERE p.estado = 1                      -- Solo productos activos (no eliminados)
                ORDER BY p.nombre ASC                   -- Ordena alfabéticamente por nombre
                """; // Fin del texto SQL

        try ( // try-with-resources: cierra los recursos automáticamente
              Connection conexion = conectar(); // Abre la conexión con la BD
              PreparedStatement ps = conexion.prepareStatement(sql); // Prepara la consulta
              ResultSet rs = ps.executeQuery() // Ejecuta la consulta y obtiene las filas resultantes
        ) { // Inicio del bloque try
            while (rs.next()) { // Avanza fila por fila mientras existan resultados
                listaProductos.add(crearProductoDesdeResultSet(rs)); // Convierte la fila en un objeto Producto y lo agrega a la lista
            } // Fin del while
            tblProducto.setItems(listaProductos); // Muestra la lista de productos en el TableView
        } catch (SQLException e) { // Si ocurre un error de base de datos...
            mostrarErrorBaseDatos("No se pudieron cargar los productos.", e); // ...muestra un mensaje de error al usuario
        } // Fin del catch
    } // Fin del método cargarProductos

    @FXML // Enlaza este método con el botón Actualizar del FXML
    private void actualizarProducto() { // Método que modifica el producto seleccionado en la tabla
        Producto productoSeleccionado = tblProducto.getSelectionModel().getSelectedItem(); // Obtiene el producto de la fila seleccionada

        if (productoSeleccionado == null) { // Si no hay ninguna fila seleccionada...
            mostrarAlerta( // ...muestra un aviso
                    Alert.AlertType.WARNING, // Tipo de alerta: advertencia
                    "Producto no seleccionado", // Título de la alerta
                    "Seleccione un producto del TableView antes de actualizar." // Mensaje explicativo
            ); // Cierra la llamada a mostrarAlerta
            return; // Sale del método sin actualizar
        } // Fin de la verificación de selección

        if (!validarFormulario()) { // Si los datos del formulario no son válidos...
            return; // ...sale del método
        } // Fin de la validación

        String codigo = txtCodigo.getText().trim(); // Lee el código del formulario sin espacios extra

        if (existeCodigo(codigo, productoSeleccionado.getIdProductos())) { // Verifica que OTRO producto (excluye el actual) no use el mismo código
            mostrarAlerta( // Muestra un aviso de duplicado
                    Alert.AlertType.WARNING, // Tipo de alerta: advertencia
                    "Código duplicado", // Título de la alerta
                    "El código ingresado pertenece a otro producto." // Mensaje explicativo
            ); // Cierra la llamada a mostrarAlerta
            txtCodigo.requestFocus(); // Coloca el cursor en el campo código
            return; // Sale del método sin actualizar
        } // Fin de la verificación de duplicado

        String sql = """
                UPDATE catalogos.productos -- Modifica un registro de la tabla productos
                SET                        -- Indica las columnas que cambiarán
                    codigo = ?,            -- Parámetro 1: nuevo código
                    nombre = ?,            -- Parámetro 2: nuevo nombre
                    descripcion = ?,       -- Parámetro 3: nueva descripción
                    precio = ?,            -- Parámetro 4: nuevo precio
                    stock = ?,             -- Parámetro 5: nuevo stock
                    fecha_creacion = ?,    -- Parámetro 6: nueva fecha
                    id_categoria = ?,      -- Parámetro 7: nueva categoría
                    id_marca = ?           -- Parámetro 8: nueva marca
                WHERE id_productos = ?     -- Parámetro 9: identifica qué producto se modifica
                """; // Fin del texto SQL

        try ( // try-with-resources
              Connection conexion = conectar(); // Abre la conexión con la BD
              PreparedStatement ps = conexion.prepareStatement(sql) // Prepara el UPDATE con parámetros
        ) { // Inicio del bloque try
            ps.setString(1, codigo); // Asigna el nuevo código
            ps.setString(2, txtProducto.getText().trim()); // Asigna el nuevo nombre
            ps.setString(3, obtenerDescripcion()); // Asigna la descripción o NULL si está vacía
            ps.setBigDecimal(4, new BigDecimal(txtPrecio.getText().trim())); // Convierte y asigna el nuevo precio
            ps.setInt(5, Integer.parseInt(txtStock.getText().trim())); // Convierte y asigna el nuevo stock
            ps.setDate(6, Date.valueOf(dpFecha.getValue())); // Convierte y asigna la nueva fecha
            ps.setInt(7, cmbCategoria.getValue().getIdCategorias()); // Asigna el ID de la nueva categoría
            ps.setInt(8, cmbMarca.getValue().getIdMarca()); // Asigna el ID de la nueva marca
            ps.setInt(9, productoSeleccionado.getIdProductos()); // Asigna el ID del producto a modificar (condición WHERE)

            int filasAfectadas = ps.executeUpdate(); // Ejecuta el UPDATE y devuelve cuántas filas cambiaron

            if (filasAfectadas > 0) { // Si se modificó el registro...
                mostrarAlerta( // ...informa al usuario
                        Alert.AlertType.INFORMATION, // Tipo de alerta: información
                        "Registro actualizado", // Título de la alerta
                        "El producto fue actualizado correctamente." // Mensaje de éxito
                ); // Cierra la llamada a mostrarAlerta
                limpiarFormulario(); // Limpia el formulario
                cargarProductos(); // Recarga la tabla con los datos actualizados
            } // Fin del if de filas afectadas
        } catch (SQLException e) { // Captura errores de la base de datos
            manejarErrorRestriccion(e, "No se pudo actualizar el producto."); // Muestra un mensaje según el tipo de error
        } // Fin del catch
    } // Fin del método actualizarProducto

    @FXML // Enlaza este método con el botón Eliminar del FXML
    private void eliminarProducto() { // Método que desactiva (eliminación lógica) el producto seleccionado
        Producto productoSeleccionado = tblProducto.getSelectionModel().getSelectedItem(); // Obtiene el producto de la fila seleccionada

        if (productoSeleccionado == null) { // Si no hay fila seleccionada...
            mostrarAlerta( // ...muestra un aviso
                    Alert.AlertType.WARNING, // Tipo de alerta: advertencia
                    "Producto no seleccionado", // Título de la alerta
                    "Seleccione un producto antes de eliminarlo." // Mensaje explicativo
            ); // Cierra la llamada a mostrarAlerta
            return; // Sale del método
        } // Fin de la verificación de selección

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION); // Crea un diálogo de confirmación para evitar eliminaciones accidentales
        confirmacion.setTitle("Confirmar eliminación"); // Título de la ventana del diálogo
        confirmacion.setHeaderText("Eliminar producto"); // Encabezado del diálogo
        confirmacion.setContentText( // Texto principal del diálogo...
                "¿Desea desactivar el producto " + productoSeleccionado.getNombre() + "?" // ...que incluye el nombre del producto
        ); // Cierra setContentText

        Optional<ButtonType> respuesta = confirmacion.showAndWait(); // Muestra el diálogo y espera el botón que pulse el usuario

        if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) { // Si cerró el diálogo o no pulsó Aceptar...
            return; // ...cancela la eliminación
        } // Fin de la verificación de la respuesta

        String sql = """
                UPDATE catalogos.productos -- Modifica la tabla productos en lugar de borrar la fila
                SET estado = 0             -- Cambia el estado a 0 (inactivo / eliminado lógicamente)
                WHERE id_productos = ?     -- Parámetro 1: ID del producto a desactivar
                """; // Fin del texto SQL

        try ( // try-with-resources
              Connection conexion = conectar(); // Abre la conexión con la BD
              PreparedStatement ps = conexion.prepareStatement(sql) // Prepara el UPDATE
        ) { // Inicio del bloque try
            ps.setInt(1, productoSeleccionado.getIdProductos()); // Asigna el ID del producto al parámetro

            int filasAfectadas = ps.executeUpdate(); // Ejecuta el UPDATE y devuelve cuántas filas cambiaron

            if (filasAfectadas > 0) { // Si se desactivó el producto...
                mostrarAlerta( // ...informa al usuario
                        Alert.AlertType.INFORMATION, // Tipo de alerta: información
                        "Producto eliminado", // Título de la alerta
                        "El producto fue desactivado correctamente." // Mensaje de éxito
                ); // Cierra la llamada a mostrarAlerta
                limpiarFormulario(); // Limpia el formulario
                cargarProductos(); // Recarga la tabla (el producto ya no aparece porque estado = 0)
            } // Fin del if de filas afectadas
        } catch (SQLException e) { // Si ocurre un error de BD...
            mostrarErrorBaseDatos("No se pudo eliminar el producto.", e); // ...muestra el mensaje de error
        } // Fin del catch
    } // Fin del método eliminarProducto

    @FXML // Enlaza este método con el botón Buscar del FXML
    private void buscarProductos() { // Método que arma una consulta dinámica según los filtros que llenó el usuario
        listaProductos.clear(); // Vacía la lista para mostrar solo los nuevos resultados

        StringBuilder sql = new StringBuilder("""
                SELECT                                  -- Inicia la consulta base
                    p.id_productos,                     -- ID del producto
                    p.codigo,                           -- Código
                    p.nombre,                           -- Nombre
                    p.descripcion,                      -- Descripción
                    p.precio,                           -- Precio
                    p.stock,                            -- Stock
                    p.fecha_creacion,                   -- Fecha de creación
                    p.id_categoria,                     -- ID de la categoría
                    c.nombre AS categoria,              -- Nombre de la categoría
                    p.id_marca,                         -- ID de la marca
                    m.nombre AS marca,                  -- Nombre de la marca
                    p.estado                            -- Estado
                FROM catalogos.productos p              -- Tabla principal de productos
                INNER JOIN catalogos.categorias c       -- Une con categorías
                    ON p.id_categoria = c.id_categorias -- Relación producto-categoría
                INNER JOIN catalogos.marcas m           -- Une con marcas
                    ON p.id_marca = m.id_marca          -- Relación producto-marca
                WHERE p.estado = 1                      -- Condición fija: nunca muestra productos eliminados
                """); // StringBuilder permite ir agregando condiciones a la consulta base

        List<Object> parametros = new ArrayList<>(); // Lista que guarda en orden los valores de cada parámetro de la consulta

        String texto = txtBuscar.getText().trim(); // Lee el texto de búsqueda sin espacios extra

        if (!texto.isBlank()) { // Si el usuario escribió algo para buscar...
            sql.append("""
                    AND (                   -- Agrega una condición agrupada
                        p.codigo ILIKE ?    -- El código contiene el texto (sin distinguir mayúsculas)
                        OR p.nombre ILIKE ? -- O el nombre contiene el texto
                    )                       -- Cierra el grupo de condiciones
                    """); // Agrega el filtro de texto a la consulta
            parametros.add("%" + texto + "%"); // Valor para el código: % permite coincidencias parciales
            parametros.add("%" + texto + "%"); // Valor para el nombre: mismo patrón de búsqueda
        } // Fin del filtro de texto

        Categoria categoria = cmbCategoriaVista2.getValue(); // Lee la categoría seleccionada en el filtro

        if (categoria != null) { // Si el usuario eligió una categoría...
            sql.append("""
                    AND p.id_categoria = ? -- Solo productos de esa categoría
                    """); // Agrega el filtro de categoría
            parametros.add(categoria.getIdCategorias()); // Guarda el ID de la categoría como parámetro
        } // Fin del filtro de categoría

        LocalDate fechaDesde = dpDesde.getValue(); // Lee la fecha inicial del rango

        if (fechaDesde != null) { // Si eligió una fecha inicial...
            sql.append("""
                    AND p.fecha_creacion >= ? -- Productos creados desde esa fecha
                    """); // Agrega el filtro de fecha inicial
            parametros.add(fechaDesde); // Guarda la fecha como parámetro
        } // Fin del filtro de fecha inicial

        LocalDate fechaHasta = dpHasta.getValue(); // Lee la fecha final del rango

        if (fechaHasta != null) { // Si eligió una fecha final...
            sql.append("""
                    AND p.fecha_creacion <= ? -- Productos creados hasta esa fecha
                    """); // Agrega el filtro de fecha final
            parametros.add(fechaHasta); // Guarda la fecha como parámetro
        } // Fin del filtro de fecha final

        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) { // Si la fecha inicial es posterior a la final...
            mostrarAlerta( // ...muestra un aviso
                    Alert.AlertType.WARNING, // Tipo de alerta: advertencia
                    "Rango de fechas incorrecto", // Título de la alerta
                    "La fecha inicial no puede ser posterior a la fecha final." // Mensaje explicativo
            ); // Cierra la llamada a mostrarAlerta
            return; // Cancela la búsqueda
        } // Fin de la validación de fechas

        BigDecimal precioMinimo = null; // Variable del precio mínimo (null = sin filtro)

        if (!txtPrecioMIN.getText().isBlank()) { // Si escribió un precio mínimo...
            try { // Intenta convertir el texto a número
                precioMinimo = new BigDecimal(txtPrecioMIN.getText().trim()); // Convierte el texto a BigDecimal

                if (precioMinimo.compareTo(BigDecimal.ZERO) < 0) { // Si el precio mínimo es negativo...
                    mostrarAlerta( // ...muestra un aviso
                            Alert.AlertType.WARNING, // Tipo de alerta: advertencia
                            "Precio incorrecto", // Título de la alerta
                            "El precio mínimo no puede ser negativo." // Mensaje explicativo
                    ); // Cierra la llamada a mostrarAlerta
                    return; // Cancela la búsqueda
                } // Fin de la validación de negativo

                sql.append("""
                        AND p.precio >= ? -- Productos con precio mayor o igual al mínimo
                        """); // Agrega el filtro de precio mínimo
                parametros.add(precioMinimo); // Guarda el precio mínimo como parámetro
            } catch (NumberFormatException e) { // Si el texto no es un número válido...
                mostrarAlerta( // ...muestra un aviso
                        Alert.AlertType.WARNING, // Tipo de alerta: advertencia
                        "Precio incorrecto", // Título de la alerta
                        "El precio mínimo debe ser numérico." // Mensaje explicativo
                ); // Cierra la llamada a mostrarAlerta
                txtPrecioMIN.requestFocus(); // Coloca el cursor en el campo
                return; // Cancela la búsqueda
            } // Fin del catch
        } // Fin del filtro de precio mínimo

        BigDecimal precioMaximo = null; // Variable del precio máximo (null = sin filtro)

        if (!txtPrecioMAX.getText().isBlank()) { // Si escribió un precio máximo...
            try { // Intenta convertir el texto a número
                precioMaximo = new BigDecimal(txtPrecioMAX.getText().trim()); // Convierte el texto a BigDecimal

                if (precioMaximo.compareTo(BigDecimal.ZERO) < 0) { // Si el precio máximo es negativo...
                    mostrarAlerta( // ...muestra un aviso
                            Alert.AlertType.WARNING, // Tipo de alerta: advertencia
                            "Precio incorrecto", // Título de la alerta
                            "El precio máximo no puede ser negativo." // Mensaje explicativo
                    ); // Cierra la llamada a mostrarAlerta
                    return; // Cancela la búsqueda
                } // Fin de la validación de negativo

                sql.append("""
                        AND p.precio <= ? -- Productos con precio menor o igual al máximo
                        """); // Agrega el filtro de precio máximo
                parametros.add(precioMaximo); // Guarda el precio máximo como parámetro
            } catch (NumberFormatException e) { // Si el texto no es un número válido...
                mostrarAlerta( // ...muestra un aviso
                        Alert.AlertType.WARNING, // Tipo de alerta: advertencia
                        "Precio incorrecto", // Título de la alerta
                        "El precio máximo debe ser numérico." // Mensaje explicativo
                ); // Cierra la llamada a mostrarAlerta
                txtPrecioMAX.requestFocus(); // Coloca el cursor en el campo
                return; // Cancela la búsqueda
            } // Fin del catch
        } // Fin del filtro de precio máximo

        if (precioMinimo != null && precioMaximo != null && precioMinimo.compareTo(precioMaximo) > 0) { // Si el mínimo es mayor que el máximo...
            mostrarAlerta( // ...muestra un aviso
                    Alert.AlertType.WARNING, // Tipo de alerta: advertencia
                    "Rango de precios incorrecto", // Título de la alerta
                    "El precio mínimo no puede ser mayor que el precio máximo." // Mensaje explicativo
            ); // Cierra la llamada a mostrarAlerta
            return; // Cancela la búsqueda
        } // Fin de la validación del rango de precios

        if (chkExiste.isSelected()) { // Si marcó "Solo productos con existencias"...
            sql.append("""
                    AND p.stock > 0 -- Excluye productos agotados
                    """); // Agrega el filtro de stock (no necesita parámetro)
        } // Fin del filtro de stock

        String orden = cmbOrdenar.getValue(); // Lee el criterio de orden elegido

        if (orden == null) { // Si no eligió ningún orden...
            orden = "Nombre"; // ...usa el orden por nombre como valor por defecto
        } // Fin de la verificación del orden

        switch (orden) { // Elige el ORDER BY de una lista fija (whitelist), nunca se concatena texto del usuario
            case "Precio menor a mayor" -> sql.append("ORDER BY p.precio ASC\n"); // Precio ascendente
            case "Precio mayor a menor" -> sql.append("ORDER BY p.precio DESC\n"); // Precio descendente
            case "Fecha más reciente" -> sql.append("ORDER BY p.fecha_creacion DESC\n"); // Fechas más nuevas primero
            case "Fecha más antigua" -> sql.append("ORDER BY p.fecha_creacion ASC\n"); // Fechas más antiguas primero
            case "Stock mayor a menor" -> sql.append("ORDER BY p.stock DESC\n"); // Mayor stock primero
            case "Stock menor a mayor" -> sql.append("ORDER BY p.stock ASC\n"); // Menor stock primero
            default -> sql.append("ORDER BY p.nombre ASC\n"); // Cualquier otro caso: orden alfabético por nombre
        } // Fin del switch

        try ( // try-with-resources
              Connection conexion = conectar(); // Abre la conexión con la BD
              PreparedStatement ps = conexion.prepareStatement(sql.toString()) // Prepara la consulta final construida
        ) { // Inicio del bloque try
            for (int i = 0; i < parametros.size(); i++) { // Recorre cada parámetro guardado en la lista
                Object valor = parametros.get(i); // Obtiene el valor en la posición i

                if (valor instanceof LocalDate fecha) { // Si el valor es una fecha...
                    ps.setDate(i + 1, Date.valueOf(fecha)); // ...lo asigna como java.sql.Date (los parámetros empiezan en 1)
                } else if (valor instanceof BigDecimal numero) { // Si el valor es un número decimal...
                    ps.setBigDecimal(i + 1, numero); // ...lo asigna como BigDecimal
                } else { // Si es otro tipo (texto o entero)...
                    ps.setObject(i + 1, valor); // ...JDBC detecta el tipo automáticamente
                } // Fin del if de tipos
            } // Fin del for de parámetros

            try (ResultSet rs = ps.executeQuery()) { // Ejecuta la consulta y cierra el ResultSet al terminar
                while (rs.next()) { // Recorre cada fila encontrada
                    listaProductos.add(crearProductoDesdeResultSet(rs)); // Convierte la fila en Producto y la agrega a la lista
                } // Fin del while
            } // Fin del try del ResultSet

            tblProducto.setItems(listaProductos); // Muestra los resultados en el TableView

            if (listaProductos.isEmpty()) { // Si no hubo resultados...
                mostrarAlerta( // ...informa al usuario
                        Alert.AlertType.INFORMATION, // Tipo de alerta: información
                        "Sin resultados", // Título de la alerta
                        "No existen productos que coincidan con los filtros aplicados." // Mensaje explicativo
                ); // Cierra la llamada a mostrarAlerta
            } // Fin del if sin resultados
        } catch (SQLException e) { // Si ocurre un error de BD...
            mostrarErrorBaseDatos("No se pudo realizar la búsqueda.", e); // ...muestra el mensaje de error
        } // Fin del catch
    } // Fin del metodo buscarProductos

    @FXML
    private void limpiarFiltros() {
        txtBuscar.clear();
        cmbCategoriaVista2.setValue(null);
        dpDesde.setValue(null);
        dpHasta.setValue(null);
        txtPrecioMIN.clear();
        txtPrecioMAX.clear();
        chkExiste.setSelected(false);
        cmbOrdenar.setValue("Nombre");
        cargarProductos();
    }

    private boolean validarFormulario() {
        if (txtCodigo.getText().isBlank()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Dato requerido", "Ingrese el código.");
            txtCodigo.requestFocus();
            return false;
        }
        if (txtCodigo.getText().trim().length() > 20) {
            mostrarAlerta(Alert.AlertType.WARNING, "Código demasiado largo", "El código admite máximo 20 caracteres.");
            txtCodigo.requestFocus();
            return false;
        }
        if (txtProducto.getText().isBlank()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Dato requerido", "Ingrese el nombre.");
            txtProducto.requestFocus();
            return false;
        }
        if (txtProducto.getText().trim().length() > 120) {
            mostrarAlerta(Alert.AlertType.WARNING, "Nombre demasiado largo", "El nombre admite máximo 120 caracteres.");
            txtProducto.requestFocus();
            return false;
        }
        if (txtDescripcion.getText() != null && txtDescripcion.getText().trim().length() > 120) {
            mostrarAlerta(Alert.AlertType.WARNING, "Descripción demasiado larga", "La descripción admite máximo 120 caracteres.");
            txtDescripcion.requestFocus();
            return false;
        }
        try {
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
            if (precio.compareTo(BigDecimal.ZERO) <= 0) {
                mostrarAlerta(Alert.AlertType.WARNING, "Precio incorrecto", "El precio debe ser mayor que cero.");
                txtPrecio.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Precio incorrecto", "El precio debe ser numérico.");
            txtPrecio.requestFocus();
            return false;
        }
        try {
            int stock = Integer.parseInt(txtStock.getText().trim());
            if (stock < 0) {
                mostrarAlerta(Alert.AlertType.WARNING, "Stock incorrecto", "El stock no puede ser negativo.");
                txtStock.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Stock incorrecto", "El stock debe ser un número entero.");
            txtStock.requestFocus();
            return false;
        }
        if (dpFecha.getValue() == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Dato requerido", "Seleccione la fecha de creación.");
            return false;
        }
        if (cmbCategoria.getValue() == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Dato requerido", "Seleccione una categoría.");
            return false;
        }
        if (cmbMarca.getValue() == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Dato requerido", "Seleccione una marca.");
            return false;
        }
        return true;
    }

    private boolean existeCodigo(String codigo, Integer idExcluir) {
        String sql = """
                SELECT 1
                FROM catalogos.productos
                WHERE codigo = ?
                AND (CAST(? AS INTEGER) IS NULL OR id_productos <> ?)
                """;

        try (
                Connection conexion = conectar();
                PreparedStatement ps = conexion.prepareStatement(sql)
        ) {
            ps.setString(1, codigo);
            ps.setObject(2, idExcluir, Types.INTEGER);
            ps.setObject(3, idExcluir, Types.INTEGER);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            mostrarErrorBaseDatos("No se pudo verificar el código.", e);
            return true;
        }
    }

    private String obtenerDescripcion() {
        String descripcion = txtDescripcion.getText();
        if (descripcion == null || descripcion.isBlank()) {
            return null;
        }
        return descripcion.trim();
    }

    private void manejarErrorRestriccion(SQLException e, String mensajeGeneral) {
        String detalle = e.getMessage() != null ? e.getMessage() : "";

        if ("23505".equals(e.getSQLState())) {
            if (detalle.contains("productos_nombre_key")) {
                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Nombre duplicado",
                        "Ya existe un producto con ese nombre."
                );
                txtProducto.requestFocus();
            } else {
                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Código duplicado",
                        "El código ingresado ya existe."
                );
                txtCodigo.requestFocus();
            }
        } else if ("23514".equals(e.getSQLState())) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Dato no permitido",
                    "Algún valor no cumple las reglas de la base de datos (precio mayor que 0, stock mayor o igual a 0)."
            );
        } else if ("22001".equals(e.getSQLState())) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Texto demasiado largo",
                    "Uno de los campos excede la longitud permitida."
            );
        } else {
            mostrarErrorBaseDatos(mensajeGeneral, e);
        }
    }

    private void limpiarFormulario() {
        txtCodigo.clear();
        txtProducto.clear();
        txtDescripcion.clear();
        txtPrecio.clear();
        txtStock.clear();
        dpFecha.setValue(LocalDate.now());
        cmbCategoria.setValue(null);
        cmbMarca.setValue(null);
        tblProducto.getSelectionModel().clearSelection();
        txtCodigo.requestFocus();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarErrorBaseDatos(String mensaje, SQLException e) {
        e.printStackTrace();
        mostrarAlerta(
                Alert.AlertType.ERROR,
                "Error de base de datos",
                mensaje + "\n\nDetalle: " + e.getMessage()
        );
    }
}