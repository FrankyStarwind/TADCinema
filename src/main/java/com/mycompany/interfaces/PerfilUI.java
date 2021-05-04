package com.mycompany.interfaces;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mycompany.components.Navegacion;
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
import com.vaadin.ui.Window;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;

@Theme("mytheme")
public class PerfilUI extends UI {

    public static boolean cambiaClave = false;
    public static String dniUsuario = "";

    @Override
    protected void init(VaadinRequest request) {
        final WrappedSession session = getSession().getSession();
        final VerticalLayout rootLayout = new VerticalLayout();
        final HorizontalLayout botonera = new HorizontalLayout();
        final VerticalLayout vInterior = new VerticalLayout();
        final Button btnLogout = new Button("Cerrar sesión");

        // comprueba si la sesión está iniciada
        comprobarSesion(rootLayout, session);

        // invalida la sesion y redirecciona a login
        btnLogout.addClickListener(e -> {
            session.invalidate();
            Page.getCurrent().setLocation("/");
        });

        // acceso a la base de datos (tabla usuarios)
        BBDD bbdd = null;
        try {
            bbdd = new BBDD("usuarios");
        } catch (UnknownHostException ex) {
            Logger.getLogger(PerfilUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        final DBCollection usuarios = bbdd.getColeccion();

        // panel para la navegación
        final Navegacion navbar = new Navegacion();
        // panel para el formulario de datos de usuario
        final Panel datosPanel = new Panel("Mis datos");

        final List<TextField> listaCampos = new ArrayList<>();

        // formulario de datos de usuario
        final FormLayout formDatos = new FormLayout();
        final TextField nombre = new TextField("Nombre");
        listaCampos.add(nombre);
        final TextField apellidos = new TextField("Apellidos");
        listaCampos.add(apellidos);
        final TextField dni = new TextField("Dni");
        listaCampos.add(dni);
        final TextField correo = new TextField("Correo");
        listaCampos.add(correo);
        final TextField username = new TextField("Usuario");
        listaCampos.add(username);
        final PasswordField password = new PasswordField("Contraseña");
        final PasswordField newPassword = new PasswordField("Nueva contraseña");
        final Button btnGuardar = new Button("Guardar datos");
        btnGuardar.setStyleName("primary");
        final Button btnEliminar = new Button("Eliminar cuenta");
        btnEliminar.setStyleName("danger");

        // carga los datos del usuario
        cargarDatosCampos(listaCampos, session, usuarios);

        // clic al botón de guardar datos
        btnGuardar.addClickListener(e -> {
            if (validarCampos(listaCampos, password, newPassword, usuarios)) {
                // Edición de los datos de usuario
                BasicDBObject actualizarUsuario = new BasicDBObject();
                // Objeto usuario
                BasicDBObject usuarioEdit = new BasicDBObject();
                usuarioEdit.append("nombre", nombre.getValue());
                usuarioEdit.append("apellidos", apellidos.getValue());
                usuarioEdit.append("_id", dni.getValue());
                usuarioEdit.append("correo", correo.getValue());
                usuarioEdit.append("username", username.getValue());
                if (cambiaClave) {
                    usuarioEdit.append("contraseña", newPassword.getValue());
                }
                // set usuario a actualizar
                actualizarUsuario.put("$set", usuarioEdit);
                // buscar por id
                BasicDBObject buscarPorId = new BasicDBObject();
                buscarPorId.append("_id", dni.getValue());
                // actualiza el elemento por id
                usuarios.update(buscarPorId, actualizarUsuario);
                Notification.show("Los datos se han guardado correctamente.", Notification.Type.TRAY_NOTIFICATION);
                // limpiar campos de contraseña
                password.setValue("");
                newPassword.setValue("");
            }
        });
        
        // botonera de confirmar/cancelar
        final HorizontalLayout botoneraPopup = new HorizontalLayout();
        final Button btnConfirmar = new Button("Eliminar");
        btnConfirmar.setStyleName("danger");
        final Button btnCancelar = new Button("Cancelar");
        botoneraPopup.addComponents(btnConfirmar, btnCancelar);
        botoneraPopup.setMargin(true);
        botoneraPopup.setSpacing(true);
        
        // ventana confirmación
        final Window ventanaConfirmacion = new Window("¿Estás seguro?");
        ventanaConfirmacion.center();
        ventanaConfirmacion.setClosable(false);
        ventanaConfirmacion.setDraggable(false);
        ventanaConfirmacion.setResizable(false);
        ventanaConfirmacion.setContent(botoneraPopup);
        
        // clic al botón eliminar
        btnEliminar.addClickListener(e -> {
            addWindow(ventanaConfirmacion);
        });
        
        // elimina el usuario al confirmar eliminación
        btnConfirmar.addClickListener(e -> {
            // busca usuario por id
            DBObject usuario = usuarios.findOne(new BasicDBObject().append("_id", dniUsuario));
            // se elimina el usuario de la tabla
            usuarios.remove(usuario);
            // invalida la sesión y redirige al login
            session.invalidate();
            Page.getCurrent().setLocation("/");
        });
        
        // cierra la ventana de confirmación
        btnCancelar.addClickListener(e -> {
            removeWindow(ventanaConfirmacion);
        });

        // ESTRUCTURA DE COMPONENTES
        botonera.addComponents(btnGuardar, btnEliminar);
        botonera.setSpacing(true);

        formDatos.addComponents(nombre, apellidos, dni, correo, username, password, newPassword, botonera);
        formDatos.setMargin(true);

        vInterior.setMargin(true);
        vInterior.addComponents(new Label("A continuación, puedes modificar tus datos en este formulario:"), formDatos);
        datosPanel.setContent(vInterior);

        rootLayout.addComponents(btnLogout, navbar, datosPanel);

        rootLayout.setMargin(true);
        rootLayout.setSpacing(true);

        setContent(rootLayout);
    }

    @WebServlet(urlPatterns = "/perfil/*", name = "PerfilUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = PerfilUI.class, productionMode = false)
    public static class PerfilUIServlet extends VaadinServlet {
    }

    /**
     * Método encargado de comprobar si la sesión existe o no Si no existe,
     * redirecciona al login
     */
    private static void comprobarSesion(final VerticalLayout rootLayout, final WrappedSession session) {
        if (session.getAttribute("usuario") == null) {
            Page.getCurrent().setLocation("/");
        } else {
            final Label bienvenido = new Label("Bienvenido, " + session.getAttribute("usuario"));
            rootLayout.addComponent(bienvenido);
        }
    }

    /**
     * Método encargado de settear los datos del usuario
     *
     * @param campos listado de campos a settear
     * @param session sesión activa
     */
    private static void cargarDatosCampos(final List<TextField> campos, final WrappedSession session,
            final DBCollection usuarios) {
        final DBCursor cursor = usuarios.find();

        DBObject itUsuario = null;
        DBObject usuario = null;
        while (cursor.hasNext()) {
            itUsuario = cursor.next();
            if (itUsuario.get("username").equals(session.getAttribute("usuario"))) {
                usuario = itUsuario;
                break;
            }
        }

        if (usuario != null) {
            for (TextField campo : campos) {
                switch (campo.getCaption()) {
                    case "Nombre":
                        campo.setValue(usuario.get("nombre").toString());
                        break;
                    case "Apellidos":
                        campo.setValue(usuario.get("apellidos").toString());
                        break;
                    case "Dni":
                        campo.setValue(usuario.get("_id").toString());
                        dniUsuario = usuario.get("_id").toString();
                        break;
                    case "Correo":
                        campo.setValue(usuario.get("correo").toString());
                        break;
                    case "Usuario":
                        campo.setValue(usuario.get("username").toString());
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * Método encargado de comprobar si los campos del formulario de datos del
     * usuario son válidos o no
     *
     * @param campos listado de campos de texto
     * @param pass campo de contraseña
     * @param newPass campo de nueva nueva contraseña
     * @return TRUE/FALSE
     */
    private static boolean validarCampos(final List<TextField> campos, final PasswordField pass,
        final PasswordField newPass, final DBCollection usuarios) {
        final DBCursor cursor = usuarios.find();
        boolean sonValidos = true;
        String clave = "";
        
        DBObject usuario = null;
        while(cursor.hasNext()) {
            usuario = cursor.next();
            if(usuario.get("_id").equals(dniUsuario)) {
                clave = usuario.get("contraseña").toString();
            }
        }

        String errores = "";

        for (TextField campo : campos) {
            switch (campo.getCaption()) {
                case "Nombre":
                    if (campo.getValue().equals("")) {
                        errores += "El campo 'Usuario' no puede estar vacío";
                        sonValidos = false;
                    }
                    break;
                case "Apellidos":
                    if (campo.getValue().equals("")) {
                        errores += "El campo 'Apellidos' no puede estar vacío";
                        sonValidos = false;
                    }
                    break;
                case "Dni":
                    if (campo.getValue().equals("")) {
                        errores += "El campo 'Dni' no puede estar vacío";
                        sonValidos = false;
                    }
                    break;
                case "Usuario":
                    if (campo.getValue().equals("")) {
                        errores += "El campo 'Usuario' no puede estar vacío";
                        sonValidos = false;
                    }
                    break;
                default:
                    break;
            }
        }

        cambiaClave = false;
        if (!pass.getValue().equals("") && newPass.getValue().equals("")) {
            errores += "Debes de escribir una nueva contraseña";
            sonValidos = false;
        } else if (pass.getValue().equals("") && !newPass.getValue().equals("")) {
            errores += "Debes de escribir tu contraseña actual";
            sonValidos = false;
        } else if (!pass.getValue().equals("") && !newPass.getValue().equals("")) {
            if (clave.equals(pass.getValue())) {
                cambiaClave = true;
            } else {
                errores += "La contraseña es incorrecta.";
                sonValidos = false;
            }
        } else {
            errores += "\n";
        }

        if (!sonValidos) {
            Notification.show("Error", errores, Notification.Type.ERROR_MESSAGE);
        }

        return sonValidos;
    }

}
