package com.mycompany.interfaces;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;

@Theme("mytheme")
public class RegisterUI extends UI {

    @Override
    protected void init(VaadinRequest request) {
        // Creación de layouts e información a modo de label
        final VerticalLayout verticalLayout = new VerticalLayout();
        final Label labelInfo = new Label("Bienvenido a TAD Cinema, para poder"
                + " registrarte debes de rellenar todos los campos del formulario");
        final FormLayout form = new FormLayout();

        // Campos requeridos del formulario
        final TextField username = new TextField("username");
        username.setRequired(true);
        final TextField name = new TextField("Nombre");
        name.setRequired(true);
        final TextField surname = new TextField("Apellidos");
        surname.setRequired(true);
        final TextField dni = new TextField("Dni");
        dni.setRequired(true);
        final TextField password = new TextField("Contraseña");
        password.setRequired(true);

        // Layout simulando un div inline
        final HorizontalLayout divButtons = new HorizontalLayout();

        // Botón para registrarse
        final Button btnRegister = new Button("Regístrate");
        btnRegister.setStyleName("primary");
        // Botón para cancelar el registro
        final Button btnCancel = new Button("Cancelar");
        btnCancel.setStyleName("danger");

        divButtons.addComponents(btnRegister, btnCancel);
        divButtons.setSpacing(true);

        // redireccion a login al pulsar el boton
        btnCancel.addClickListener(e -> {
            Page.getCurrent().setLocation("/login");
        });

        // crea un nuevo registro de usuario
        btnRegister.addClickListener(e -> {
            if (camposValidos(name, surname, dni, password)) {
                try {
                    // creación del cliente de mongo
                    MongoClient mongoClient = new MongoClient("localhost", 27017);

                    DB db = mongoClient.getDB("TADCinemaDB"); // obtención de la base de datos
                    System.out.println("Conectado a la base de datos");

                    // si no existe, se crea
                    if (!existeUsuario(dni, db)) {
                        // creación del documento usuario
                        BasicDBObject usuario = new BasicDBObject();
                        usuario.append("username", username.getValue());
                        usuario.append("nombre", name.getValue());
                        usuario.append("apellidos", surname.getValue());
                        usuario.append("_id", dni.getValue());
                        usuario.append("contraseña", password.getValue());

                        // Obtengo la colección de los usuarios
                        DBCollection usuarios = db.getCollection("usuarios");
                        usuarios.insert(usuario);

                        // resetea valores
                        resetearCampos(name, surname, dni, password);

                        // mensaje de éxito
                        verticalLayout.addComponent(new Label("<p style=\"color: green; "
                                + "font-weight: bold;\">Registro de usuario realizado correctamente.</p>", ContentMode.HTML));
                    } else {
                        // mensaje de error
                        verticalLayout.addComponent(new Label("<p style=\"color: red; "
                                + "font-weight: bold;\">El usuario ya existe en base de datos.</p>", ContentMode.HTML));
                    }
                } catch (Exception ex) {
                    System.err.println(ex.getClass().getName() + ": " + ex.getMessage());
                }
            }
        });

        // Se añaden los componentes al formulario
        form.addComponents(username,password,name, surname, dni,  divButtons);

        verticalLayout.addComponents(labelInfo, form);
        verticalLayout.setMargin(true);
        verticalLayout.setSpacing(true);

        setContent(verticalLayout);
    }

    @WebServlet(urlPatterns = "/registro/*", name = "RegisterUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = RegisterUI.class, productionMode = false)
    public static class RegisterUIServlet extends VaadinServlet {
    }

    /**
     * Método encargado de comprobar que los campos del registro son válidos
     *
     * @param name nombre
     * @param surname apellidos
     * @param dni dni
     * @param password contraseña
     * @return TRUE/FALSE
     */
    public static boolean camposValidos(TextField name, TextField surname, TextField dni, TextField password) {
        boolean esCorrecto = true;

        List<String> errores = new ArrayList<>();

        if (name.getValue() == "") {
            esCorrecto = false;
            errores.add("El campo 'Nombre' es obligatorio.");
        }
        if (surname.getValue() == "") {
            esCorrecto = false;
            errores.add("El campo 'Apellidos' es obligatorio.");
        }
        if (dni.getValue() == "") {
            esCorrecto = false;
            errores.add("El campo 'Dni' es obligatorio.");
        }
        if (password.getValue() == "") {
            esCorrecto = false;
            errores.add("El campo 'Contraseña' es obligatorio.");
        }

        if (!esCorrecto) {
            String salidaError = "";
            for (String error : errores) {
                salidaError += error + "\n";
            }
            Notification notification = new Notification("Error", salidaError, Notification.Type.ERROR_MESSAGE);
            notification.show(Page.getCurrent());
        }

        return esCorrecto;
    }

    /**
     * Método encargado de resetear los valores de los campos del formulario de
     * registro
     *
     * @param name nombre
     * @param surname apellidos
     * @param dni dni
     * @param password contraseña
     */
    public static void resetearCampos(TextField name, TextField surname, TextField dni, TextField password) {
        name.setValue("");
        surname.setValue("");
        dni.setValue("");
        password.setValue("");
    }

    /**
     * Método encargado de comprobar si existe el usuario en base de datos
     *
     * @param dni dni del usuario introducido en el formulario
     * @param db base de datos
     * @return TRUE/FALSE
     */
    public static boolean existeUsuario(TextField dni, DB db) {
        boolean existe = false;

        // obtengo la colección de los usuarios
        DBCollection equipos = db.getCollection("usuarios");

        // cursor para iterar la lista de usuarios
        final DBCursor cursor = equipos.find();

        DBObject usuario;
        // recorre la lista y si lo encuentra, sale del bucle
        while (cursor.hasNext()) {
            usuario = cursor.next();
            if (usuario.get("_id").equals(dni.getValue())) {
                existe = true;
                break;
            }
        }

        return existe;
    }

}
