package com.desarrollo.proyectocrudg3.Controller;

public class ProductoController {
    // Estos son los métodos iniciales para empezar a trabajar

    private void configurarTableView(){
        colCodigo.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getCodigo()
                ));
        colNombre.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getNombre()
                )
        );
        colCategoria.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getCategoria()
                )
        );
    /*colDescripcion.setCellValueFactory(data ->
            new SimpleStringProperty(
                    data.getValue().getDescripcion()
            )
    );*/
        colMarca.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getMarca()
                )
        );
        colPrecio.setCellValueFactory(data ->
                new SimpleDoubleProperty(
                        data.getValue().getPrecio()
                ).asObject()
        );
        colStock.setCellValueFactory( data ->
                new SimpleIntegerProperty(
                        data.getValue().getStock()
                ).asObject()
        );
        colFecha.setCellValueFactory(data ->
                new SimpleObjectProperty<>(
                        data.getValue().getFechaCreacion()
                )
        );

        tblProductos.setItems(listaProductos);
    }


// Selección

    private void configurarSeleccionTableView(){
        tblProductos.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if(newValue != null){
                        cargarProductoEnFormulario(newValue);
                    }
                });
    }



    /**
     * Recibe como parámetro un producto seleccionado del TableView
     * @param p, es el producto seleccionado del TableView
     */
    private void cargarProductoEnFormulario(Producto p){
        txtCodigo.setText(p.getCodigo());
        txtNombre.setText(p.getNombre());
        txtDescripcion.setText(p.getDescripcion());
        txtPrecio.setText(String.valueOf(p.getPrecio()));
        txtStock.setText(String.valueOf(p.getStock()));
        dpFechaCreacion.setValue(p.getFechaCreacion());

        /**
         * Buscamos dentro del ComboBox el valor que corresponda al ID
         */

        for(Categoria categoria: listaCategorias){
            if(categoria.getIdCategoria() == p.getIdCategoria()){
                cmbCategoria.setValue(categoria);
                break;
            }
        }

        /**
         * Buscamos dentro del ComboBox el valor que corresponda al ID
         */
        for(Marca marca: listaMarcas){
            if(marca.getIdMarca() == p.getIdMarca()){
                cmbMarca.setValue(marca);
                break;
            }
        }
    }

    /**
     * Consulta las categorias activas de la base de datos para mostrarlas dentro del ComboBox
     */
    private void cargarCategorias() {

        listaCategorias.clear();

        String sql = """
            SELECT
                id_categoria,
                nombre
            FROM catalogos.categorias
            WHERE estado = 1
            ORDER BY nombre
            """;

        try (
                Connection conexion =
                        ConexionBD.getConnection();

                PreparedStatement ps =
                        conexion.prepareStatement(sql);

                ResultSet rs =
                        ps.executeQuery()
        ) {

            while (rs.next()) {

                Categoria categoria =
                        new Categoria(
                                rs.getInt("id_categoria"),
                                rs.getString("nombre")
                        );

                listaCategorias.add(categoria);
            }


            /*
             * Utilizamos copias de la lista para evitar que
             * cada ComboBox tenga problemas de selección.
             */
            cmbCategoria.setItems(
                    FXCollections.observableArrayList(
                            listaCategorias
                    )
            );

            cmbFiltroCategoria.setItems(
                    FXCollections.observableArrayList(
                            listaCategorias
                    )
            );

        } catch (SQLException e) {

            mostrarErrorBaseDatos(
                    "No se pudieron cargar las categorías.",
                    e
            );
        }
    }


    /**
     * Consulta las marcas activas almacenadas en PostgreSQL.
     *
     * Estas marcas se agregan al ComboBox utilizado en
     * el formulario de productos.
     */
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
                Connection conexion =
                        ConexionBD.getConnection();

                PreparedStatement ps =
                        conexion.prepareStatement(sql);

                ResultSet rs =
                        ps.executeQuery()
        ) {

            while (rs.next()) {

                Marca marca =
                        new Marca(
                                rs.getInt("id_marca"),
                                rs.getString("nombre")
                        );

                listaMarcas.add(marca);
            }

            cmbMarca.setItems(
                    FXCollections.observableArrayList(
                            listaMarcas
                    )
            );

        } catch (SQLException e) {

            mostrarErrorBaseDatos(
                    "No se pudieron cargar las marcas.",
                    e
            );
        }
    }

    /**
     * Agrega al ComboBox las opciones disponibles para
     * ordenar los resultados.
     *
     * El usuario no introduce directamente el nombre
     * de una columna SQL.
     *
     * Esto permite utilizar una whitelist de valores
     * conocidos y evita concatenar texto arbitrario
     * dentro de ORDER BY.
     */
    private void cargarOpcionesOrden() {

        cmbOrden.setItems(
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

        cmbOrden.setValue("Nombre");
    }

// Crear producto desde resultset permite escribir los objetos coomo filas

    private Producto crearProductoDesdeResultSet(
            ResultSet rs
    ) throws SQLException {

        return new Producto(

                rs.getInt(
                        "id_producto"
                ),

                rs.getString(
                        "codigo"
                ),

                rs.getString(
                        "nombre"
                ),

                rs.getString(
                        "descripcion"
                ),

                rs.getDouble(
                        "precio"
                ),

                rs.getInt(
                        "stock"
                ),

                rs.getDate(
                        "fecha_creacion"
                ).toLocalDate(),

                rs.getInt(
                        "id_categoria"
                ),

                rs.getString(
                        "categoria"
                ),

                rs.getInt(
                        "id_marca"
                ),

                rs.getString(
                        "marca"
                ),

                rs.getInt(
                        "estado"
                )
        );
    }
    @FXML
    private void guardarProducto() {

        if (!validarFormulario()) {
            return;
        }



        String codigo =
                txtCodigo.getText().trim();



        /*
         * Validación preventiva de UNIQUE.
         */
        if (existeCodigo(codigo, null)) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Código duplicado",
                    "El código " + codigo +
                            " ya pertenece a otro producto."
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
                Connection conexion =
                        ConexionBD.getConnection();

                PreparedStatement ps =
                        conexion.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    codigo
            );

            ps.setString(
                    2,
                    txtNombre.getText().trim()
            );

            ps.setString(
                    3,
                    txtDescripcion.getText().trim()
            );

            ps.setBigDecimal(
                    4,
                    new BigDecimal(
                            txtPrecio.getText().trim()
                    )
            );

            ps.setInt(
                    5,
                    Integer.parseInt(
                            txtStock.getText().trim()
                    )
            );

            ps.setDate(
                    6,
                    Date.valueOf(
                            dpFechaCreacion.getValue()
                    )
            );

            ps.setInt(
                    7,
                    cmbCategoria.getValue()
                            .getIdCategoria()
            );

            ps.setInt(
                    8,
                    cmbMarca.getValue()
                            .getIdMarca()
            );



            int filasAfectadas =
                    ps.executeUpdate();



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

            /*
             * Aunque JavaFX realizó previamente la comprobación,
             * PostgreSQL continúa siendo la autoridad final.
             *
             * SQLState 23505 corresponde normalmente a una
             * violación de UNIQUE en PostgreSQL.
             */
            if ("23505".equals(e.getSQLState())) {

                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Registro duplicado",
                        "El código ingresado ya existe."
                );

            } else {

                mostrarErrorBaseDatos(
                        "No se pudo guardar el producto.",
                        e
                );
            }
        }
    }

    @FXML
    private void actualizarProducto() {

        Producto productoSeleccionado =
                tblProductos.getSelectionModel()
                        .getSelectedItem();


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


        String codigo =
                txtCodigo.getText().trim();


        /*
         * Se comprueba que otro producto diferente
         * no esté utilizando el mismo código.
         */
        if (existeCodigo(
                codigo,
                productoSeleccionado.getIdProducto()
        )) {

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

                WHERE id_producto = ?
                """;


        try (
                Connection conexion =
                        ConexionBD.getConnection();

                PreparedStatement ps =
                        conexion.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    codigo
            );

            ps.setString(
                    2,
                    txtNombre.getText().trim()
            );

            ps.setString(
                    3,
                    txtDescripcion.getText().trim()
            );

            ps.setBigDecimal(
                    4,
                    new BigDecimal(
                            txtPrecio.getText().trim()
                    )
            );

            ps.setInt(
                    5,
                    Integer.parseInt(
                            txtStock.getText().trim()
                    )
            );

            ps.setDate(
                    6,
                    Date.valueOf(
                            dpFechaCreacion.getValue()
                    )
            );

            ps.setInt(
                    7,
                    cmbCategoria.getValue()
                            .getIdCategoria()
            );

            ps.setInt(
                    8,
                    cmbMarca.getValue()
                            .getIdMarca()
            );

            ps.setInt(
                    9,
                    productoSeleccionado.getIdProducto()
            );


            int filasAfectadas =
                    ps.executeUpdate();


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

            if ("23505".equals(e.getSQLState())) {

                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Código duplicado",
                        "El código ingresado ya está siendo utilizado."
                );

            } else {

                mostrarErrorBaseDatos(
                        "No se pudo actualizar el producto.",
                        e
                );
            }
        }
    }

    @FXML
    private void eliminarProducto() {

        Producto productoSeleccionado =
                tblProductos.getSelectionModel()
                        .getSelectedItem();


        if (productoSeleccionado == null) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Producto no seleccionado",
                    "Seleccione un producto antes de eliminarlo."
            );

            return;
        }


        /*
         * Se solicita confirmación para evitar que el usuario
         * desactive accidentalmente un registro.
         */
        Alert confirmacion =
                new Alert(Alert.AlertType.CONFIRMATION);

        confirmacion.setTitle(
                "Confirmar eliminación"
        );

        confirmacion.setHeaderText(
                "Eliminar producto"
        );

        confirmacion.setContentText(
                "¿Desea desactivar el producto "
                        + productoSeleccionado.getNombre()
                        + "?"
        );


        Optional<ButtonType> respuesta =
                confirmacion.showAndWait();


        if (
                respuesta.isEmpty()
                        ||
                        respuesta.get() != ButtonType.OK
        ) {

            return;
        }


        String sql = """
                UPDATE catalogos.productos
                SET estado = 0
                WHERE id_producto = ?
                """;


        try (
                Connection conexion =
                        ConexionBD.getConnection();

                PreparedStatement ps =
                        conexion.prepareStatement(sql)
        ) {

            ps.setInt(
                    1,
                    productoSeleccionado.getIdProducto()
            );


            int filasAfectadas =
                    ps.executeUpdate();


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

            mostrarErrorBaseDatos(
                    "No se pudo eliminar el producto.",
                    e
            );
        }
    }
    @FXML
    private void buscarProductos() {
        listaProductos.clear();

        /*
         * La consulta inicia con los INNER JOIN necesarios
         * y con una condición permanente:
         *
         * p.estado = 1
         *
         * De esta forma nunca mostramos productos
         * eliminados lógicamente.
         */
        StringBuilder sql = new StringBuilder("""
                SELECT
                    p.id_producto,
                    p.codigo,
                    p.nombre,
                    p.descripcion,
                    p.precio,
                    p.stock,
                    p.fecha_creacion,
                    c.id_categoria,
                    c.nombre AS categoria,
                    m.id_marca,
                    m.nombre AS marca,
                    p.estado
                FROM catalogos.productos p
                INNER JOIN catalogos.categorias c
                    ON p.id_categoria = c.id_categoria
                INNER JOIN catalogos.marcas m
                    ON p.id_marca = m.id_marca
                WHERE p.estado = 1
                """);



        /*
         * Aquí almacenaremos los valores que posteriormente
         * serán enviados al PreparedStatement.
         */
        List<Object> parametros =
                new ArrayList<>();

        String texto =
                txtFiltroTexto.getText().trim();



        if (!texto.isBlank()) {

            /*
             * OR permite que el texto coincida con código
             * o nombre.
             *
             * ILIKE permite búsquedas parciales sin importar
             * mayúsculas y minúsculas.
             */
            sql.append("""
                   
                    AND (
                        p.codigo ILIKE ?
                        OR p.nombre ILIKE ?
                    )
                    """);



            parametros.add(
                    "%" + texto + "%"
            );

            parametros.add(
                    "%" + texto + "%"
            );
        }

        Categoria categoria =
                cmbFiltroCategoria.getValue();



        if (categoria != null) {

            sql.append("""
                   
                    AND p.id_categoria = ?
                    """);

            parametros.add(
                    categoria.getIdCategoria()
            );
        }

        LocalDate fechaDesde =
                dpFiltroDesde.getValue();



        if (fechaDesde != null) {

            sql.append("""
                   
                    AND p.fecha_creacion >= ?
                    """);

            parametros.add(fechaDesde);
        }

        LocalDate fechaHasta =
                dpFiltroHasta.getValue();



        if (fechaHasta != null) {

            sql.append("""
                   
                    AND p.fecha_creacion <= ?
                    """);

            parametros.add(fechaHasta);
        }



        /*
         * Verificamos que las fechas tengan sentido.
         *
         * "Desde" no debería ser posterior a "Hasta".
         */
        if (
                fechaDesde != null
                        &&
                        fechaHasta != null
                        &&
                        fechaDesde.isAfter(fechaHasta)
        ) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Rango de fechas incorrecto",
                    "La fecha inicial no puede ser posterior a la fecha final."
            );

            return;
        }

        BigDecimal precioMinimo = null;



        if (!txtPrecioMin.getText().isBlank()) {

            try {

                precioMinimo =
                        new BigDecimal(
                                txtPrecioMin
                                        .getText()
                                        .trim()
                        );



                if (
                        precioMinimo.compareTo(
                                BigDecimal.ZERO
                        ) < 0
                ) {

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

                txtPrecioMin.requestFocus();

                return;
            }
        }

        BigDecimal precioMaximo = null;



        if (!txtPrecioMax.getText().isBlank()) {

            try {

                precioMaximo =
                        new BigDecimal(
                                txtPrecioMax
                                        .getText()
                                        .trim()
                        );



                if (
                        precioMaximo.compareTo(
                                BigDecimal.ZERO
                        ) < 0
                ) {

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

                txtPrecioMax.requestFocus();

                return;
            }
        }



        /*
         * Validamos que el precio mínimo no sea superior
         * al precio máximo.
         */
        if (
                precioMinimo != null
                        &&
                        precioMaximo != null
                        &&
                        precioMinimo.compareTo(
                                precioMaximo
                        ) > 0
        ) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Rango de precios incorrecto",
                    "El precio mínimo no puede ser mayor que el precio máximo."
            );

            return;
        }



        if (chkConStock.isSelected()) {

            sql.append("""
                   
                    AND p.stock > 0
                    """);
        }




        String orden =
                cmbOrden.getValue();



        if (orden == null) {

            orden = "Nombre";
        }



        switch (orden) {

            case "Precio menor a mayor" ->
                    sql.append("""
                           
                            ORDER BY p.precio ASC
                            """);

            case "Precio mayor a menor" ->
                    sql.append("""
                           
                            ORDER BY p.precio DESC
                            """);

            case "Fecha más reciente" ->
                    sql.append("""
                           
                            ORDER BY p.fecha_creacion DESC
                            """);

            case "Fecha más antigua" ->
                    sql.append("""
                           
                            ORDER BY p.fecha_creacion ASC
                            """);

            case "Stock mayor a menor" ->
                    sql.append("""
                           
                            ORDER BY p.stock DESC
                            """);

            case "Stock menor a mayor" ->
                    sql.append("""
                           
                            ORDER BY p.stock ASC
                            """);

            default ->
                    sql.append("""
                           
                            ORDER BY p.nombre ASC
                            """);
        }



        try (
                Connection conexion =
                        ConexionBD.getConnection();

                PreparedStatement ps =
                        conexion.prepareStatement(
                                sql.toString()
                        )
        ) {

            /*
             * Recorremos la lista de parámetros para
             * asignarlos dinámicamente al PreparedStatement.
             */
            for (
                    int i = 0;
                    i < parametros.size();
                    i++
            ) {

                Object valor =
                        parametros.get(i);



                if (valor instanceof LocalDate fecha) {

                    ps.setDate(
                            i + 1,
                            Date.valueOf(fecha)
                    );

                } else if (valor instanceof BigDecimal numero) {

                    ps.setBigDecimal(
                            i + 1,
                            numero
                    );

                } else {

                    ps.setObject(
                            i + 1,
                            valor
                    );
                }
            }



            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                while (rs.next()) {

                    listaProductos.add(
                            crearProductoDesdeResultSet(rs)
                    );
                }
            }



            tblProductos.setItems(
                    listaProductos
            );

            if (listaProductos.isEmpty()) {

                mostrarAlerta(
                        Alert.AlertType.INFORMATION,
                        "Sin resultados",
                        "No existen productos que coincidan con los filtros aplicados."
                );
            }

        } catch (SQLException e) {

            mostrarErrorBaseDatos(
                    "No se pudo realizar la búsqueda.",
                    e
            );
        }
    }
}