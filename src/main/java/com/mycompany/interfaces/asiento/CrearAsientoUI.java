package com.mycompany.interfaces.asiento;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mycompany.components.Navegacion;
import com.mycompany.utils.BBDD;
import com.mycompany.interfaces.sala.CrearSalaUI;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.WrappedSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;

@Theme("mytheme")
public class CrearAsientoUI extends UI {

    @Override
    protected void init(VaadinRequest request) {
        final WrappedSession session = getSession().getSession();
        final VerticalLayout rootLayout = new VerticalLayout();
        final Button btnLogout = new Button("Cerrar sesión");

        // comprueba si se ha iniciado sesión
        comprobarSesion(rootLayout, session);

        // invalida la sesion y redirecciona a login
        btnLogout.addClickListener(e -> {
            session.invalidate();
            Page.getCurrent().setLocation("/");
        });

        // panel de navegación
        final Navegacion navbar = new Navegacion();

        // panel de crear asiento
        final Panel panelCreate = new Panel("Crear nuevo asiento");
        final VerticalLayout layoutCreate = new VerticalLayout();

        // formulario crear asiento
        final FormLayout formCreate = new FormLayout();
        final ComboBox tipo = new ComboBox("Tipo de asiento", comboTipos());
        tipo.setRequired(true);
        tipo.setInputPrompt("Selecciona tipo");
        final TextField fila = new TextField("Fila asiento");
        fila.setRequired(true);
        fila.setInputPrompt("Introduce la fila");
        final TextField numero = new TextField("Número de asiento");
        numero.setRequired(true);
        numero.setInputPrompt("Introduce el número");

        // botón para crear asiento
        final Button btnCrear = new Button("Crear asiento");
        btnCrear.setStyleName("primary");

        formCreate.addComponents(tipo, fila, numero, btnCrear);
        formCreate.setMargin(true);

        layoutCreate.addComponents(new Label("Por favor, rellene todos los campos para añadir un nuevo asiento."), formCreate);
        layoutCreate.setMargin(true);
        layoutCreate.setSpacing(true);

        panelCreate.setContent(layoutCreate);

        // crea un asiento nuevo 
        btnCrear.addClickListener(e -> {
            if (validarCampos(tipo, fila, numero)) {
                try {
                    BBDD bbdd = new BBDD("asientos");

                    DBCollection asientos = bbdd.getColeccion();

                    BasicDBObject asiento = new BasicDBObject();
                    asiento.append("_id", fila.getValue() + "_" +  numero.getValue());
                    asiento.append("tipo", tipo.getValue());
                    asiento.append("fila", fila.getValue());
                    asiento.append("numero", numero.getValue());

                    DBObject query = new BasicDBObject().append("_id", fila.getValue() + "_" +  numero.getValue());
                    DBObject spot = asientos.findOne(query);

                    if (spot == null) {
                        asientos.insert(asiento);
                        resetarCampos(tipo, fila, numero);
                        Notification.show("Se ha creado el asiento correctamente", Notification.Type.TRAY_NOTIFICATION);
                    } else {
                        Notification.show("El asiento ya existe", Notification.Type.ERROR_MESSAGE);
                    }
                } catch (UnknownHostException ex) {
                    Logger.getLogger(CrearSalaUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        // ESTRUCTURA DE LA INTERFAZ
        rootLayout.addComponents(btnLogout, navbar, panelCreate);

        rootLayout.setMargin(true);
        rootLayout.setSpacing(true);

        setContent(rootLayout);
    }

    @WebServlet(urlPatterns = "/crear-asiento/*", name = "CrearAsientoUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = CrearAsientoUI.class, productionMode = false)
    public static class CrearAsientoUIServlet extends VaadinServlet {
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
     * Método encargado de cargar el combo de tipo de asiento
     *
     * @return Listado de tipos de asiento
     */
    private static List<String> comboTipos() {
        List<String> tipos = new ArrayList<>();
        tipos.add("Predeterminado");
        tipos.add("Minusválidos");
        tipos.add("Reclinable");
        return tipos;
    }
    
    /**
     * Método encargado de validar los campos del formulario
     * @param tipo Tipo de asiento
     * @param fila Fila del asiento
     * @param numero Número del asiento
     * @return TRUE/FALSE
     */
    private static boolean validarCampos(ComboBox tipo, TextField fila, TextField numero) {
        boolean validos = true;
        String errores = "";
        
        if (tipo.getValue() == null) {
            errores += "El campo 'Tipo de asiento' no puede estar vacío\n";
            validos = false;
        }
        if (fila.getValue() == "") {
            errores += "El campo 'Fila asiento' es obligatorio\n";
            validos = false;
        }
        if (numero.getValue() == "") {
            errores += "El campo 'Número de asiento' es obligatorio\n";
            validos = false;
        }
        
        if (!validos) {
            Notification.show("Error", errores, Notification.Type.ERROR_MESSAGE);
        }
        
        return validos;
    }
    
    /**
     * Método encargado de resetear los campos del formulario
     * @param tipo Tipo de asiento
     * @param fila Fila del asiento
     * @param numero Número del asiento
     */
    private static void resetarCampos(ComboBox tipo, TextField fila, TextField numero) {
        tipo.setValue(null);
        fila.setValue("");
        numero.setValue("");
    }

}
