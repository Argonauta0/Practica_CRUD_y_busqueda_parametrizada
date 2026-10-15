module com.desarrollo.proyectocrudg3 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;


    opens com.desarrollo.proyectocrudg3 to javafx.fxml;
    opens com.desarrollo.proyectocrudg3.Controller to javafx.fxml;
    opens com.desarrollo.proyectocrudg3.Model to javafx.fxml;

    exports com.desarrollo.proyectocrudg3;
}