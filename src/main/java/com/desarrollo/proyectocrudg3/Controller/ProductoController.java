package com.desarrollo.proyectocrudg3.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

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

