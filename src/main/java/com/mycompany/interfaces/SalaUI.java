package com.mycompany.interfaces;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mycompany.components.Navegacion;
import com.mycompany.model.Compra;
import com.mycompany.utils.BBDD;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.WrappedSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.io.File;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;

@Theme("mytheme")
public class SalaUI extends UI {

    public String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();

    public static List<Integer> filas = new ArrayList<Integer>();
    public static List<Integer> numeros = new ArrayList<Integer>();

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

        // panel de contenido
        final Panel contenido = new Panel();
        final VerticalLayout vLayout = new VerticalLayout();

        String nombreSesion = session.getAttribute("nombrePeli").toString();
        String salaSesion = session.getAttribute("sala").toString();
        String sesHora = session.getAttribute("hora").toString();

        final Label nomPeli = new Label(nombreSesion + " - Sala " + salaSesion + " (" + sesHora + ")");

        rellenarSala();

        final ComboBox comboFilas = new ComboBox("Fila", filas);
        comboFilas.setRequired(true);
        comboFilas.setInputPrompt("Selecciona la fila");

        final ComboBox comboAsientos = new ComboBox("Número de asiento", numeros);
        comboAsientos.setRequired(true);
        comboAsientos.setInputPrompt("Selecciona el asiento");

        final Button btnComprar = new Button("Comprar");
        btnComprar.setStyleName("primary");

        final Image image = new Image(null, new FileResource(new File(basepath + "/WEB-INF/images/sala.JPG")));

        vLayout.addComponents(nomPeli, image, comboFilas, comboAsientos, btnComprar);
        vLayout.setMargin(true);
        vLayout.setSpacing(true);
        contenido.setContent(vLayout);

        btnComprar.addClickListener(e -> {
            if (comboAsientos.getValue() != null && comboFilas.getValue() != null) {
                int asiento = (int) comboAsientos.getValue();
                int fila = (int) comboFilas.getValue();

                if (comprobarDisponibilidad(fila, asiento, salaSesion)) {
                    String nUser = session.getAttribute("usuario").toString();
                    String nPeli = session.getAttribute("nombrePeli").toString();
                    String hora = session.getAttribute("hora").toString();
                    Compra compra = new Compra(nUser, nPeli, fila, asiento, hora);

                    session.setAttribute("compra", compra);
                    Page.getCurrent().setLocation("/compraEntrada");
                } else {
                    Notification.show("El asiento ya está ocupado. Por favor, seleccione otro diferente.", Notification.Type.ERROR_MESSAGE);
                }
            }
        });

        rootLayout.addComponents(btnLogout, navbar, contenido);
        rootLayout.setMargin(true);
        rootLayout.setSpacing(true);

        setContent(rootLayout);
    }

    @WebServlet(urlPatterns = "/sala/*", name = "SalaUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = SalaUI.class, productionMode = false)
    public static class SalaUIServlet extends VaadinServlet {
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
     * Método encargado de rellenar los asientos de la sala
     */
    private static void rellenarSala() {
        try {
            final BBDD bbdd = new BBDD("asientos");
            final DBCollection asientos = bbdd.getColeccion();
            final DBCursor cursor = asientos.find();

            DBObject asiento = null;
            while (cursor.hasNext()) {
                asiento = cursor.next();
                Integer fila = Integer.valueOf(asiento.get("fila").toString());
                Integer numero = Integer.valueOf(asiento.get("numero").toString());
                if (!filas.contains(fila)) {
                    filas.add(fila);
                }
                if (!numeros.contains(numero)) {
                    numeros.add(numero);
                }
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(SalaUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Método encargado de comprobar si el asiento escogido por el cliente está
     * ya ocupado o no
     *
     * @param fila fila del asiento
     * @param numero número del asiento
     * @param sala número de la sala
     * @return TRUE/FALSE
     */
    private static boolean comprobarDisponibilidad(Integer fila, Integer numero, String sala) {
        boolean disponible = true;

        BBDD bbdd = null;
        try {
            bbdd = new BBDD("asientos");
        } catch (UnknownHostException ex) {
            Logger.getLogger(SalaUI.class.getName()).log(Level.SEVERE, null, ex);
        }

        final DBCollection asientos = bbdd.getColeccion();
        BasicDBObject asientoSala = new BasicDBObject().append("sala", sala);
        final DBCursor cursor = asientos.find(asientoSala);

        DBObject asiento = null;
        while (cursor.hasNext()) {
            asiento = cursor.next();
            if (asiento.get("fila").equals(fila) && asiento.get("numero").equals(numero)) {
                if (asiento.get("disponible").equals("Si")) {
                    disponible = true;
                } else {
                    disponible = false;
                }

                break;
            }
        }

        return disponible;
    }

}
