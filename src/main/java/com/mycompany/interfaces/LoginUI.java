package com.mycompany.interfaces;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mycompany.utils.BBDD;
import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.WrappedSession;
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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Theme("mytheme")
public class LoginUI extends UI {

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        // obtengo la sesion
        final WrappedSession session = getSession().getSession();

        // Layouts, panel y el label de información
        final VerticalLayout verticalLayout = new VerticalLayout();
        final Label labelInfo = new Label("Inicia sesión para entrar a las funcionalidades de la aplicación");
        final Panel loginPanel = new Panel("Inicio de sesión");
        final FormLayout form = new FormLayout();

        // Campos usuario y contraseña
        final TextField username = new TextField("Usuario");
        final PasswordField password = new PasswordField("Contraseña");

        // Layout simulando un div inline
        final HorizontalLayout divButtons = new HorizontalLayout();

        // Botón de inicio de sesión
        final Button btnLogin = new Button("Iniciar sesión");
        btnLogin.setStyleName("primary");

        // Botón de registro
        final Button btnRegister = new Button("Regístrate");

        divButtons.addComponents(btnLogin, btnRegister);
        divButtons.setSpacing(true);

        // iniciar sesión en el sistema
        btnLogin.addClickListener(e -> {
            if (camposValidos(username, password)) {
                try {
                    BBDD db = new BBDD("usuarios");
                    
                    if (existeUsuario(username, password, db.getColeccion(), session)) {
                        session.setAttribute("usuario", username.getValue());

                        Page.getCurrent().setLocation("/home");
                    }
                } catch (UnknownHostException ex) {
                    Logger.getLogger(LoginUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        // redireccion a registro al pulsar el boton
        btnRegister.addClickListener(e -> {
            Page.getCurrent().setLocation("/registro");
        });

        // Se añaden los componentes al formulario
        form.addComponents(username, password, divButtons);
        form.setSpacing(true);
        form.setMargin(true);

        loginPanel.setContent(form);
        loginPanel.setWidth("440px");

        verticalLayout.addComponents(labelInfo, loginPanel);
        verticalLayout.setMargin(true);
        verticalLayout.setSpacing(true);

        setContent(verticalLayout);
    }

    @WebServlet(urlPatterns = "/*", name = "LoginUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = LoginUI.class, productionMode = false)
    public static class LoginUIServlet extends VaadinServlet {
    }

    /**
     * Método encargado de comprobar si existe el usuario en base de datos
     *
     * @param username nombre de usuario
     * @param password contraseña
     * @param usuarios tabla de usuarios
     * @param session
     * @return TRUE/FALSE
     */
//    WrappedSession session = getSession().getSession();
    public static boolean existeUsuario(TextField username, PasswordField password,
            DBCollection usuarios, WrappedSession session) {
        boolean existe = false;

        // cursor para iterar la lista de usuarios
        final DBCursor cursor = usuarios.find();

        DBObject usuario;
        // recorre la lista y si lo encuentra, sale del bucle
        while (cursor.hasNext()) {
            usuario = cursor.next();
            if (usuario.get("username").equals(username.getValue())) {
                if (usuario.get("contraseña").equals(password.getValue())) {
                    existe = true;
                    session.setAttribute("rol",usuario.get("rol"));
                    break;
                } else {
                    Notification.show("Error", "Contraseña no válida", Notification.Type.ERROR_MESSAGE);
                }
            } else {
                Notification.show("Error", "Usuario no válido", Notification.Type.ERROR_MESSAGE);
            }
        }

        return existe;

    }

    /**
     * Método encargado de comprobar que los campos sean válidos
     *
     * @param username campo de usuario
     * @param password campo de contraseña
     * @return TRUE/FALSE
     */
    public static boolean camposValidos(TextField username, PasswordField password) {
        boolean esCorrecto = true;

        List<String> errores = new ArrayList<>();

        if (username.getValue() == "") {
            esCorrecto = false;
            errores.add("El campo 'Usuario' es obligatorio.");
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
}
