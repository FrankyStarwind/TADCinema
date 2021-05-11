package com.mycompany.interfaces.usuario;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mycompany.utils.BBDD;
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
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
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
        // Creación de layouts, panel e información a modo de label
        final VerticalLayout verticalLayout = new VerticalLayout();
        final Label labelInfo = new Label("Bienvenido a TAD Cinema, para poder"
                + " registrarte debes de rellenar todos los campos del formulario");
        final Panel registerPanel = new Panel("Formulario de registro");
        final FormLayout form = new FormLayout();

        // lista para almacenar los campos
        List<TextField> campos = new ArrayList<>();

        // Campos requeridos del formulario
        final TextField name = new TextField("Nombre");
        name.setRequired(true);
        campos.add(name);
        final TextField surname = new TextField("Apellidos");
        surname.setRequired(true);
        campos.add(surname);
        final TextField dni = new TextField("Dni");
        dni.setRequired(true);
        campos.add(dni);
        final TextField correo = new TextField("Correo");
        correo.setRequired(false);
        campos.add(correo);
        final TextField username = new TextField("Usuario");
        username.setRequired(true);
        campos.add(username);
        final PasswordField password = new PasswordField("Contraseña");
        password.setRequired(true);
        
        final Label infoOblig = new Label("<p><span style=\"color: #ed473b;\">*"
                + "</span> Campo obligatorio</p>", ContentMode.HTML);

        // Layout simulando un div inline
        final HorizontalLayout divButtons = new HorizontalLayout();

        // Botón para registrarse
        final Button btnRegister = new Button("Regístrate");
        btnRegister.setStyleName("primary");
        // Botón para cancelar el registro
        final Button btnCancel = new Button("Volver");
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
                    BBDD bbdd = new BBDD("usuarios");
                    System.out.println("Conectado a la base de datos");

                    // si no existe, se crea
                    if (!existeUsuario("_id", dni, bbdd.getColeccion()) && !existeUsuario("username", username, bbdd.getColeccion())) {
                        // creación del documento usuario
                        BasicDBObject usuario = new BasicDBObject();
                        usuario.append("nombre", name.getValue());
                        usuario.append("apellidos", surname.getValue());
                        usuario.append("_id", dni.getValue());
                        usuario.append("correo", correo.getValue());
                        usuario.append("username", username.getValue());
                        usuario.append("contraseña", password.getValue());
                        usuario.append("rol", "cliente");

                        // Obtengo la colección de los usuarios
                        DBCollection usuarios = bbdd.getColeccion();
                        usuarios.insert(usuario);

                        // resetea valores
                        resetearCampos(campos, password);

                        // mensaje de éxito
                        verticalLayout.addComponent(new Label("<p style=\"color: green; "
                                + "font-weight: bold;\">Registro de usuario realizado correctamente.</p>", ContentMode.HTML));
                    } else if (existeUsuario("_id", dni, bbdd.getColeccion())) {
                        // mensaje de error
                        verticalLayout.addComponent(new Label("<p style=\"color: red; "
                                + "font-weight: bold;\">El dni ya existe en base de datos.</p>", ContentMode.HTML));
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
        form.addComponents(name, surname, dni, correo, username, password, infoOblig, divButtons);
        form.setMargin(true);
        form.setSpacing(true);
        
        registerPanel.setContent(form);
        registerPanel.setWidth("420px");

        verticalLayout.addComponents(labelInfo, registerPanel);
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
    public static boolean camposValidos(TextField name, TextField surname, TextField dni, PasswordField password) {
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
     * @param campos listado de campos
     */
    public static void resetearCampos(List<TextField> campos, PasswordField password) {
        for (TextField campo : campos) {
            campo.setValue("");
        }
        
        password.setValue("");
    }

    /**
     * Método encargado de comprobar si existe el usuario en base de datos
     *
     * @param nombreCampo tipo de campo
     * @param campo campo introducido en el formulario
     * @param usuarios tabla de usuarios
     * @return TRUE/FALSE
     */
    public static boolean existeUsuario(String nombreCampo, TextField campo, DBCollection usuarios) {
        boolean existe = false;

        // cursor para iterar la lista de usuarios
        final DBCursor cursor = usuarios.find();

        DBObject usuario;

        switch (nombreCampo) {
            case "_id":
                // recorre la lista y si lo encuentra, sale del bucle
                while (cursor.hasNext()) {
                    usuario = cursor.next();
                    if (usuario.get("_id").equals(campo.getValue())) {
                        existe = true;
                        break;
                    }
                }
                break;
            case "username":
                // recorre la lista y si lo encuentra, sale del bucle
                while (cursor.hasNext()) {
                    usuario = cursor.next();
                    if (usuario.get("username").equals(campo.getValue())) {
                        existe = true;
                        break;
                    }
                }
                break;
            default:
                break;
        }

        return existe;
    }

}
