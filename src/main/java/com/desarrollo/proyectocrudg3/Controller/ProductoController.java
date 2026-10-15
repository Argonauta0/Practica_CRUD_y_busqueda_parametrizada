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

    @FXML
    private void guardarProducto() {
        if (!validarFormulario()) {
            return;
        }

        String codigo = txtCodigo.getText().trim();

        if (existeCodigo(codigo, null)) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Código duplicado",
                    "El código " + codigo + " ya pertenece a otro producto."
            );
            txtCodigo.requestFocus();
            return;
        }

        String sql = """
                INSERT INTO catalogos.productos
                (
                    codigo,
                    nombre,
                    descripcion,
                    precio,
                    stock,
                    fecha_creacion,
                    id_categoria,
                    id_marca,
                    estado
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1)
                """;

        try (
                Connection conexion = conectar();
                PreparedStatement ps = conexion.prepareStatement(sql)
        ) {
            ps.setString(1, codigo);
            ps.setString(2, txtProducto.getText().trim());
            ps.setString(3, obtenerDescripcion());
            ps.setBigDecimal(4, new BigDecimal(txtPrecio.getText().trim()));
            ps.setInt(5, Integer.parseInt(txtStock.getText().trim()));
            ps.setDate(6, Date.valueOf(dpFecha.getValue()));
            ps.setInt(7, cmbCategoria.getValue().getIdCategorias());
            ps.setInt(8, cmbMarca.getValue().getIdMarca());

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                mostrarAlerta(
                        Alert.AlertType.INFORMATION,
                        "Registro guardado",
                        "El producto fue registrado correctamente."
                );
                limpiarFormulario();
                cargarProductos();
            }
        } catch (SQLException e) {
            manejarErrorRestriccion(e, "No se pudo guardar el producto.");
        }
    }

    private void cargarProductos() {
        listaProductos.clear();

        String sql = """
                SELECT
                    p.id_productos,
                    p.codigo,
                    p.nombre,
                    p.descripcion,
                    p.precio,
                    p.stock,
                    p.fecha_creacion,
                    p.id_categoria,
                    c.nombre AS categoria,
                    p.id_marca,
                    m.nombre AS marca,
                    p.estado
                FROM catalogos.productos p
                INNER JOIN catalogos.categorias c
                    ON p.id_categoria = c.id_categorias
                INNER JOIN catalogos.marcas m
                    ON p.id_marca = m.id_marca
                WHERE p.estado = 1
                ORDER BY p.nombre ASC
                """;

        try (
                Connection conexion = conectar();
                PreparedStatement ps = conexion.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                listaProductos.add(crearProductoDesdeResultSet(rs));
            }
            tblProducto.setItems(listaProductos);
        } catch (SQLException e) {
            mostrarErrorBaseDatos("No se pudieron cargar los productos.", e);
        }
    }

    @FXML
    private void actualizarProducto() {
        Producto productoSeleccionado = tblProducto.getSelectionModel().getSelectedItem();

        if (productoSeleccionado == null) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Producto no seleccionado",
                    "Seleccione un producto del TableView antes de actualizar."
            );
            return;
        }

        if (!validarFormulario()) {
            return;
        }

        String codigo = txtCodigo.getText().trim();

        if (existeCodigo(codigo, productoSeleccionado.getIdProductos())) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Código duplicado",
                    "El código ingresado pertenece a otro producto."
            );
            txtCodigo.requestFocus();
            return;
        }

        String sql = """
                UPDATE catalogos.productos
                SET
                    codigo = ?,
                    nombre = ?,
                    descripcion = ?,
                    precio = ?,
                    stock = ?,
                    fecha_creacion = ?,
                    id_categoria = ?,
                    id_marca = ?
                WHERE id_productos = ?
                """;

        try (
                Connection conexion = conectar();
                PreparedStatement ps = conexion.prepareStatement(sql)
        ) {
            ps.setString(1, codigo);
            ps.setString(2, txtProducto.getText().trim());
            ps.setString(3, obtenerDescripcion());
            ps.setBigDecimal(4, new BigDecimal(txtPrecio.getText().trim()));
            ps.setInt(5, Integer.parseInt(txtStock.getText().trim()));
            ps.setDate(6, Date.valueOf(dpFecha.getValue()));
            ps.setInt(7, cmbCategoria.getValue().getIdCategorias());
            ps.setInt(8, cmbMarca.getValue().getIdMarca());
            ps.setInt(9, productoSeleccionado.getIdProductos());

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                mostrarAlerta(
                        Alert.AlertType.INFORMATION,
                        "Registro actualizado",
                        "El producto fue actualizado correctamente."
                );
                limpiarFormulario();
                cargarProductos();
            }
        } catch (SQLException e) {
            manejarErrorRestriccion(e, "No se pudo actualizar el producto.");
        }
    }

    @FXML
    private void eliminarProducto() {
        Producto productoSeleccionado = tblProducto.getSelectionModel().getSelectedItem();

        if (productoSeleccionado == null) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Producto no seleccionado",
                    "Seleccione un producto antes de eliminarlo."
            );
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("Eliminar producto");
        confirmacion.setContentText(
                "¿Desea desactivar el producto " + productoSeleccionado.getNombre() + "?"
        );

        Optional<ButtonType> respuesta = confirmacion.showAndWait();

        if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) {
            return;
        }

        String sql = """
                UPDATE catalogos.productos
                SET estado = 0
                WHERE id_productos = ?
                """;

        try (
                Connection conexion = conectar();
                PreparedStatement ps = conexion.prepareStatement(sql)
        ) {
            ps.setInt(1, productoSeleccionado.getIdProductos());

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                mostrarAlerta(
                        Alert.AlertType.INFORMATION,
                        "Producto eliminado",
                        "El producto fue desactivado correctamente."
                );
                limpiarFormulario();
                cargarProductos();
            }
        } catch (SQLException e) {
            mostrarErrorBaseDatos("No se pudo eliminar el producto.", e);
        }
    }

    @FXML
    private void buscarProductos() {
        listaProductos.clear();

        StringBuilder sql = new StringBuilder("""
                SELECT
                    p.id_productos,
                    p.codigo,
                    p.nombre,
                    p.descripcion,
                    p.precio,
                    p.stock,
                    p.fecha_creacion,
                    p.id_categoria,
                    c.nombre AS categoria,
                    p.id_marca,
                    m.nombre AS marca,
                    p.estado
                FROM catalogos.productos p
                INNER JOIN catalogos.categorias c
                    ON p.id_categoria = c.id_categorias
                INNER JOIN catalogos.marcas m
                    ON p.id_marca = m.id_marca
                WHERE p.estado = 1
                """);

        List<Object> parametros = new ArrayList<>();

        String texto = txtBuscar.getText().trim();

        if (!texto.isBlank()) {
            sql.append("""
                    AND (
                        p.codigo ILIKE ?
                        OR p.nombre ILIKE ?
                    )
                    """);
            parametros.add("%" + texto + "%");
            parametros.add("%" + texto + "%");
        }

        Categoria categoria = cmbCategoriaVista2.getValue();

        if (categoria != null) {
            sql.append("""
                    AND p.id_categoria = ?
                    """);
            parametros.add(categoria.getIdCategorias());
        }

        LocalDate fechaDesde = dpDesde.getValue();

        if (fechaDesde != null) {
            sql.append("""
                    AND p.fecha_creacion >= ?
                    """);
            parametros.add(fechaDesde);
        }

        LocalDate fechaHasta = dpHasta.getValue();

        if (fechaHasta != null) {
            sql.append("""
                    AND p.fecha_creacion <= ?
                    """);
            parametros.add(fechaHasta);
        }

        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Rango de fechas incorrecto",
                    "La fecha inicial no puede ser posterior a la fecha final."
            );
            return;
        }

        BigDecimal precioMinimo = null;

        if (!txtPrecioMIN.getText().isBlank()) {
            try {
                precioMinimo = new BigDecimal(txtPrecioMIN.getText().trim());

                if (precioMinimo.compareTo(BigDecimal.ZERO) < 0) {
                    mostrarAlerta(
                            Alert.AlertType.WARNING,
                            "Precio incorrecto",
                            "El precio mínimo no puede ser negativo."
                    );
                    return;
                }

                sql.append("""
                        AND p.precio >= ?
                        """);
                parametros.add(precioMinimo);
            } catch (NumberFormatException e) {
                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Precio incorrecto",
                        "El precio mínimo debe ser numérico."
                );
                txtPrecioMIN.requestFocus();
                return;
            }
        }

        BigDecimal precioMaximo = null;

        if (!txtPrecioMAX.getText().isBlank()) {
            try {
                precioMaximo = new BigDecimal(txtPrecioMAX.getText().trim());

                if (precioMaximo.compareTo(BigDecimal.ZERO) < 0) {
                    mostrarAlerta(
                            Alert.AlertType.WARNING,
                            "Precio incorrecto",
                            "El precio máximo no puede ser negativo."
                    );
                    return;
                }

                sql.append("""
                        AND p.precio <= ?
                        """);
                parametros.add(precioMaximo);
            } catch (NumberFormatException e) {
                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Precio incorrecto",
                        "El precio máximo debe ser numérico."
                );
                txtPrecioMAX.requestFocus();
                return;
            }
        }

        if (precioMinimo != null && precioMaximo != null && precioMinimo.compareTo(precioMaximo) > 0) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Rango de precios incorrecto",
                    "El precio mínimo no puede ser mayor que el precio máximo."
            );
            return;
        }

        if (chkExiste.isSelected()) {
            sql.append("""
                    AND p.stock > 0
                    """);
        }

        String orden = cmbOrdenar.getValue();

        if (orden == null) {
            orden = "Nombre";
        }

        switch (orden) {
            case "Precio menor a mayor" -> sql.append("ORDER BY p.precio ASC\n");
            case "Precio mayor a menor" -> sql.append("ORDER BY p.precio DESC\n");
            case "Fecha más reciente" -> sql.append("ORDER BY p.fecha_creacion DESC\n");
            case "Fecha más antigua" -> sql.append("ORDER BY p.fecha_creacion ASC\n");
            case "Stock mayor a menor" -> sql.append("ORDER BY p.stock DESC\n");
            case "Stock menor a mayor" -> sql.append("ORDER BY p.stock ASC\n");
            default -> sql.append("ORDER BY p.nombre ASC\n");
        }

        try (
                Connection conexion = conectar();
                PreparedStatement ps = conexion.prepareStatement(sql.toString())
        ) {
            for (int i = 0; i < parametros.size(); i++) {
                Object valor = parametros.get(i);

                if (valor instanceof LocalDate fecha) {
                    ps.setDate(i + 1, Date.valueOf(fecha));
                } else if (valor instanceof BigDecimal numero) {
                    ps.setBigDecimal(i + 1, numero);
                } else {
                    ps.setObject(i + 1, valor);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    listaProductos.add(crearProductoDesdeResultSet(rs));
                }
            }

            tblProducto.setItems(listaProductos);

            if (listaProductos.isEmpty()) {
                mostrarAlerta(
                        Alert.AlertType.INFORMATION,
                        "Sin resultados",
                        "No existen productos que coincidan con los filtros aplicados."
                );
            }
        } catch (SQLException e) {
            mostrarErrorBaseDatos("No se pudo realizar la búsqueda.", e);
        }
    }

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