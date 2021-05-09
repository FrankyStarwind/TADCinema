package com.mycompany.interfaces.sala;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mycompany.components.Navegacion;
import com.mycompany.utils.BBDD;
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
public class CrearSalaUI extends UI {

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

        // panel de crear sala
        final Panel panelCreate = new Panel("Crear nueva sala");
        final VerticalLayout layoutCreate = new VerticalLayout();

        // formulario crear sala
        final FormLayout formCreate = new FormLayout();
        final ComboBox numero = new ComboBox("Nº sala", comboNumeros());
        numero.setRequired(true);
        numero.setInputPrompt("Selecciona número");
        final TextField filas = new TextField("Filas");
        filas.setRequired(true);
        filas.setInputPrompt("Número de filas");
        final TextField asientos = new TextField("Asientos");
        asientos.setRequired(true);
        asientos.setInputPrompt("Número de asientos");
        final ComboBox tipoSala = new ComboBox("Tipo de sala", comboTipos());
        tipoSala.setRequired(true);
        tipoSala.setInputPrompt("Selecciona el tipo");

        // botón para crear sala
        final Button btnCrear = new Button("Crear sala");
        btnCrear.setStyleName("primary");

        formCreate.addComponents(numero, filas, asientos, tipoSala, btnCrear);
        formCreate.setMargin(true);

        layoutCreate.addComponents(new Label("Por favor, rellene todos los campos para añadir una nueva sala."), formCreate);
        layoutCreate.setMargin(true);
        layoutCreate.setSpacing(true);

        panelCreate.setContent(layoutCreate);

        // crea una sala nueva
        // genera también los asientos de dicha sala
        btnCrear.addClickListener(e -> {
            if (validarCampos(numero, filas, asientos, tipoSala)) {
                try {
                    BBDD bbdd = new BBDD("salas");

                    final DBCollection salas = bbdd.getColeccion();
                    
                    Integer numFilas = Integer.parseInt(filas.getValue());
                    Integer numAsientos = Integer.parseInt(asientos.getValue());

                    final BasicDBObject sala = new BasicDBObject();
                    sala.append("_id", numero.getValue());
                    sala.append("capacidad", calculaCapacidad(numFilas, numAsientos));
                    sala.append("tipo", tipoSala.getValue());

                    final DBObject query = new BasicDBObject().append("_id", numero.getValue());
                    final DBObject data = salas.findOne(query);

                    if (data == null) {
                        salas.insert(sala);
                        
                        // genera automáticamente los asientos de dicha sala
                        bbdd = new BBDD("asientos");
                        final DBCollection tablaAsientos = bbdd.getColeccion();
                        
                        String idAsiento = "";
                        BasicDBObject asiento = new BasicDBObject();
                        
                        for (int fila = 1; fila <= numFilas; fila++) {
                            for (int col = 1; col <= numAsientos; col++) {
                                idAsiento = sala.get("_id").toString()+ "-" +fila + "-" + col;
                                
                                asiento.append("_id", idAsiento);
                                asiento.append("tipo", "Predeterminado");
                                asiento.append("fila", fila);
                                asiento.append("numero", col);
                                asiento.append("sala", sala.get("_id"));
                                asiento.append("disponible", "Si");
                                
                                tablaAsientos.insert(asiento);
                            }
                        }
                        
                        resetarCampos(numero, filas, asientos, tipoSala);
                        Notification.show("Se ha creado la sala correctamente", Notification.Type.TRAY_NOTIFICATION);
                        Page.getCurrent().setLocation("/salas");
                    } else {
                        Notification.show("La sala ya existe", Notification.Type.ERROR_MESSAGE);
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

    @WebServlet(urlPatterns = "/crear-sala/*", name = "CrearSalaUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = CrearSalaUI.class, productionMode = false)
    public static class CrearSalaUIServlet extends VaadinServlet {
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
     * Método encargado de cargar el combo de números de sala
     *
     * @return Listado de números del 1 al 12
     */
    private static List<String> comboNumeros() {
        final List<String> numeros = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            numeros.add(String.valueOf(i));
        }
        return numeros;
    }

    /**
     * Método encargado de cargar el combo de tipos de sala
     *
     * @return Listado de tipos de sala
     */
    private static List<String> comboTipos() {
        final List<String> tipos = new ArrayList<>();
        tipos.add("Predeterminada");
        tipos.add("3D");
        tipos.add("iMax");
        return tipos;
    }

    /**
     * Método encargado de validar los campos del formulario
     *
     * @param numero Número de la sala
     * @param filas Filas de asientos
     * @param asientos Número de asientos por fila
     * @param tipoSala Tipo de sala
     * @return TRUE/FALSE
     */
    private static boolean validarCampos(ComboBox numero, TextField filas, TextField asientos, ComboBox tipoSala) {
        boolean validos = true;
        String errores = "";

        if (numero.getValue() == null) {
            errores += "El campo 'Nº sala' no puede estar vacío\n";
            validos = false;
        }
        if (filas.getValue() == "") {
            errores += "El campo 'Filas' es obligatorio\n";
            validos = false;
        }
        if (asientos.getValue() == "") {
            errores += "El campo 'Asientos' es obligatorio\n";
            validos = false;
        }
        if (tipoSala.getValue() == null) {
            errores += "El campo 'Tipo de sala' no puede estar vacío";
            validos = false;
        }

        if (!validos) {
            Notification.show("Error", errores, Notification.Type.ERROR_MESSAGE);
        }

        return validos;
    }

    /**
     * Método encargado de resetear los campos del formulario
     *
     * @param numero Número de sala
     * @param filas Filas de asientos
     * @param asientos Número de asientos por fila
     * @param tipoSala Tipo de sala
     */
    private static void resetarCampos(ComboBox numero, TextField filas, TextField asientos, ComboBox tipoSala) {
        numero.setValue(null);
        filas.setValue("");
        asientos.setValue("");
        tipoSala.setValue(null);
    }

    private int calculaCapacidad(int filas, int asientos) {
        return filas * asientos;
    }
}
