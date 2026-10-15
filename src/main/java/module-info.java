module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql; // Obligatorio para conectarse a la base de datos PostgreSQL

    // Permite a JavaFX iniciar la aplicación
    opens com.example.demo to javafx.fxml;

    // Permite a JavaFX inyectar los botones y campos de texto en el controlador
    opens com.example.demo.controller to javafx.fxml;

    // Permite a la tabla leer los datos de la clase Empleado
    opens com.example.demo.model to javafx.base;

    exports com.example.demo;
}