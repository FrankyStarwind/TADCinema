package com.mycompany.interfaces;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
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

    // Nombre de clase de la interfaz completa
    public static final String CLASSNAME = "login";

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        // Layout general con css
        final CssLayout layout = new CssLayout();
        layout.setStyleName(CLASSNAME);

        // Layouts vertical y formulario y el label de información
        final VerticalLayout verticalLayout = new VerticalLayout();
        final Label labelInfo = new Label("Inicia sesión para entrar a las funcionalidades de la aplicación");
        final FormLayout form = new FormLayout();

        // Campos usuario y contraseña
        final TextField username = new TextField("Usuario");
        final TextField password = new TextField("Contraseña");

        List<TextField> componentesLogin = new ArrayList<TextField>();
        componentesLogin.add(username);
        componentesLogin.add(password);

        // Layout simulando un div inline
        final HorizontalLayout divButtons = new HorizontalLayout();

        // Botón de inicio de sesión
        final Button btnLogin = new Button("Iniciar sesión");
        btnLogin.setStyleName("primary");

        // Botón de registro
        final Button btnRegister = new Button("Regístrate");

        divButtons.addComponents(btnLogin, btnRegister);
        divButtons.setSpacing(true);

        //
        btnLogin.addClickListener(e -> {
            if (camposValidos(componentesLogin)) {

                MongoClient mongoClient;
                try {
                    mongoClient = new MongoClient("localhost", 27017);
                    DB db = mongoClient.getDB("TADCinemaDB");
                    if (existeUsuario(username, password, db)) {
                        Page.getCurrent().setLocation("/Cartelera");
                    } else {
                        Notification.show("Usuario/contraseña no válidas", Notification.Type.ERROR_MESSAGE);
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
        form.setStyleName(CLASSNAME + "-form");

        verticalLayout.addComponents(labelInfo, form);
        verticalLayout.setMargin(true);
        verticalLayout.setSpacing(true);

        layout.addComponent(verticalLayout);

        setContent(layout);
    }

    @WebServlet(urlPatterns = "/*", name = "LoginUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = LoginUI.class, productionMode = false)
    public static class LoginUIServlet extends VaadinServlet {
    }

    public static boolean existeUsuario(TextField username, TextField password,
            DB db) {
        boolean existe = false;

        // obtengo la colección de los usuarios
        DBCollection equipos = db.getCollection("usuarios");

        // cursor para iterar la lista de usuarios
        final DBCursor cursor = equipos.find();

        DBObject usuario;
        // recorre la lista y si lo encuentra, sale del bucle
        while (cursor.hasNext()) {
            usuario = cursor.next();
            if (usuario.get("username").equals(username.getValue())) {
                if (usuario.get("password").equals(password.getValue())) {
                    existe = true;
                }
                break;
            }
        }

        return existe;

    }

    public static boolean camposValidos(List<TextField> listaComponentes) {
        boolean esCorrecto = true;

        List<String> errores = new ArrayList<>();

        if (listaComponentes.get(0).getValue() == "") {
            esCorrecto = false;
            errores.add("El campo 'Usuario' es obligatorio.");
        }
        if (listaComponentes.get(1).getValue() == "") {
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
