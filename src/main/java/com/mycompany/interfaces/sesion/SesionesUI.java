package com.mycompany.interfaces.sesion;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mycompany.components.Navegacion;
import com.mycompany.utils.BBDD;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.WrappedSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;

@Theme("mytheme")
public class SesionesUI extends UI {

    private final static List<String> listadoPeliculas = new ArrayList<>();
    private static DBCollection sesiones = null;
    private static Object idSelected = null;

    @Override
    protected void init(VaadinRequest request) {
        final WrappedSession session = getSession().getSession();
        final VerticalLayout rootLayout = new VerticalLayout();
        final Button btnLogout = new Button("Cerrar sesión");
        final GridLayout grid = new GridLayout(2, 1);

        // comprueba si se ha iniciado sesión
        comprobarSesion(rootLayout, session);

        // invalida la sesion y redirecciona a login
        btnLogout.addClickListener(e -> {
            session.invalidate();
            Page.getCurrent().setLocation("/");
        });

        // panel de navegación
        final Navegacion navbar = new Navegacion();

        // botón crear sesión (redirige a la ui determinada)
        final HorizontalLayout botoneraCrear = new HorizontalLayout();
        final Button btnCrear = new Button("Crear sesión");
        botoneraCrear.addComponent(btnCrear);
        // al pulsar el botón de crear
        btnCrear.addClickListener(e -> {
            Page.getCurrent().setLocation("/crear-sesion");
        });

        // tabla de sesiones
        final Table tablaSesiones = obtenerTabla();

        // panel de edición
        final Panel panelEdit = new Panel("Gestión de la sesión");
        final VerticalLayout vLayout = new VerticalLayout();
        final Label info = new Label("Para facilitar la edición, puedes seleccionar"
                + " un registro de la tabla y luego editarlo.");

        // formulario edición
        final FormLayout form = new FormLayout();

        final ComboBox pelicula = new ComboBox("Película", listadoPeliculas);
        pelicula.setRequired(true);
        pelicula.setInputPrompt("Selecciona película");
        final ComboBox sala = new ComboBox("Nº sala", comboSalas());
        sala.setInputPrompt("Selecciona la sala");
        final ComboBox hora = new ComboBox("Horario", comboHoras());
        hora.setInputPrompt("Selecciona una hora");
        final Button btnEditar = new Button("Modificar");
        btnEditar.setStyleName("primary");
        final Button btnEliminar = new Button("Eliminar");
        btnEliminar.setStyleName("danger");

        final HorizontalLayout hLayout = new HorizontalLayout();
        hLayout.addComponents(btnEditar, btnEliminar);
        hLayout.setSpacing(true);

        form.addComponents(pelicula, sala, hora, hLayout);
        form.setMargin(true);

        vLayout.addComponents(info, form);
        vLayout.setMargin(true);
        panelEdit.setContent(vLayout);

        // Estructura del grid
        grid.addComponent(tablaSesiones, 0, 0);
        grid.addComponent(panelEdit, 1, 0);
        grid.setSizeFull();
        grid.setSpacing(true);

        // si selecciona un registro de la tabla
        // se añaden los datos al formulario
        tablaSesiones.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                final DBCursor cursor = sesiones.find();

                DBObject sesion = null;
                while (cursor.hasNext()) {
                    sesion = cursor.next();
                    if (sesion.get("_id").equals(event.getItemId())) {
                        pelicula.setValue(sesion.get("pelicula"));
                        sala.setValue(Integer.valueOf(sesion.get("sala").toString()));
                        hora.setValue(sesion.get("hora"));
                        break;
                    }
                }
            }
        });

        // al pulsar el botón de editar
        btnEditar.addClickListener(e -> {
            if (Objects.nonNull(pelicula.getValue()) && (Objects.nonNull(sala.getValue()) || Objects.nonNull(hora.getValue()))) {
                BasicDBObject sesion = new BasicDBObject();
                if (sala.getValue() != null) {
                    sesion.append("sala", sala.getValue());
                }
                if (hora.getValue() != null) {
                    sesion.append("hora", hora.getValue());
                }

                // sesión a actualizar
                BasicDBObject sesionUpdate = new BasicDBObject();
                sesionUpdate.put("$set", sesion);
                // buscar por id
                BasicDBObject buscarPorId = new BasicDBObject();
                buscarPorId.append("_id", idSelected);
                // actualiza el elemento por id
                sesiones.update(buscarPorId, sesionUpdate);
                Notification.show("Los datos se han modificado correctamente.", Notification.Type.TRAY_NOTIFICATION);
                // limpiar campos
                resetarCampos(pelicula, sala, hora);
                // actualizamos la tabla
                actualizarTabla(tablaSesiones);
            } else if (Objects.nonNull(pelicula.getValue())) {
                Notification.show("Debes rellenar algún campo más.", Notification.Type.ERROR_MESSAGE);
            } else {
                Notification.show("El campo 'Película' es obligatorio.", Notification.Type.ERROR_MESSAGE);
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

        // al pulsar el botón de eliminar
        btnEliminar.addClickListener(e -> {
            if (Objects.nonNull(pelicula.getValue())) {
                addWindow(ventanaConfirmacion);
            } else {
                Notification.show("Primero debes de seleccionar una película", Notification.Type.ERROR_MESSAGE);
            }
        });

        // al pulsar el botón de confirmar eliminación
        btnConfirmar.addClickListener(e -> {
            // Obtengo la sesión
            DBObject sesion = sesiones.findOne(new BasicDBObject().append("_id", idSelected));

            if (Objects.nonNull(sesion)) {
                // Elimino la sesión
                sesiones.remove(sesion);

                // actualizo tabla y elimino ventana
                actualizarTabla(tablaSesiones);
                removeWindow(ventanaConfirmacion);

                Notification.show("El registro se ha eliminado correctamente", Notification.Type.TRAY_NOTIFICATION);
            }
        });

        // al pulsar el botón de cancelar
        btnCancelar.addClickListener(e -> {
            removeWindow(ventanaConfirmacion);
        });

        // ESTRUCTURA DE LA INTERFAZ
        rootLayout.addComponents(btnLogout, navbar, botoneraCrear, grid);

        rootLayout.setMargin(true);
        rootLayout.setSpacing(true);

        setContent(rootLayout);
    }

    @WebServlet(urlPatterns = "/sesiones/*", name = "SesionesUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = SesionesUI.class, productionMode = false)
    public static class SesionesUIServlet extends VaadinServlet {
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
     * Método encargado de obtener la lista de sesiones general, crear una tabla
     * con ella y devolverla
     *
     * @return Tabla de asientos
     */
    private static Table obtenerTabla() {
        final Table tabla = new Table();
        tabla.addContainerProperty("Película", String.class, null);
        tabla.addContainerProperty("Sala", Integer.class, null);
        tabla.addContainerProperty("Hora", String.class, null);

        BBDD bbdd = null;
        try {
            bbdd = new BBDD("sesiones");
        } catch (UnknownHostException ex) {
            Logger.getLogger(SesionesUI.class.getName()).log(Level.SEVERE, null, ex);
        }

        sesiones = bbdd.getColeccion();
        final DBCursor cursor = sesiones.find();

        DBObject sesion = null;
        while (cursor.hasNext()) {
            sesion = cursor.next();
            idSelected = sesion.get("_id");
            String pelicula = sesion.get("pelicula").toString();
            Integer sala = Integer.valueOf(sesion.get("sala").toString());
            String hora = sesion.get("hora").toString();
            tabla.addItem(new Object[]{pelicula, sala, hora}, idSelected);
            listadoPeliculas.add(pelicula);
        }

        tabla.setSelectable(true);
        tabla.setSizeFull();
        return tabla;
    }

    /**
     * Método encargado de actualizar la tabla de sesiones
     *
     * @param tabla Tabla de sesiones
     */
    private static void actualizarTabla(Table tabla) {
        tabla.removeAllItems();
        listadoPeliculas.clear();
        final DBCursor cursor = sesiones.find();

        DBObject sesion = null;
        while (cursor.hasNext()) {
            sesion = cursor.next();
            Object id = sesion.get("_id");
            String pelicula = sesion.get("pelicula").toString();
            Integer sala = Integer.valueOf(sesion.get("sala").toString());
            String hora = sesion.get("hora").toString();
            tabla.addItem(new Object[]{pelicula, sala, hora}, id);
            listadoPeliculas.add(pelicula);
        }
    }

    /**
     * Método encargado de devolver la lista de salas existentes
     *
     * @return Listado de salas en bbdd
     */
    private static List<Integer> comboSalas() {
        final List<Integer> salas = new ArrayList<>();
        BBDD bbdd = null;
        try {
            bbdd = new BBDD("salas");
        } catch (UnknownHostException ex) {
            Logger.getLogger(CrearSesionUI.class.getName()).log(Level.SEVERE, null, ex);
        }

        final DBCollection data = bbdd.getColeccion();
        final DBCursor cursor = data.find();

        DBObject sala = null;
        while (cursor.hasNext()) {
            sala = cursor.next();
            salas.add(Integer.valueOf(sala.get("_id").toString()));
        }
        
        salas.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });

        return salas;
    }

    /**
     * Método encargado de devolver una lista de horarios disponibles
     *
     * @return Listado horarios
     */
    private static List<String> comboHoras() {
        List<String> horas = new ArrayList<>();
        horas.add("12:00");
        horas.add("12:30");
        horas.add("13:15");
        horas.add("14:10");
        horas.add("16:00");
        horas.add("16:50");
        horas.add("17:30");
        horas.add("18:10");
        horas.add("19:00");
        horas.add("19:45");
        horas.add("20:30");
        horas.add("21:00");
        horas.add("21:35");
        horas.add("22:15");
        horas.add("22:45");
        horas.add("23:10");
        horas.add("23:50");
        horas.add("00:35");
        horas.add("01:15");
        return horas;
    }

    /**
     * Método encargado de resetear los campos del formulario
     *
     * @param pelicula Película de la sesión
     * @param sala Sala de la sesión
     * @param hora Hora de la sesión
     */
    private static void resetarCampos(ComboBox pelicula, ComboBox sala, ComboBox hora) {
        pelicula.setValue(null);
        sala.setValue(null);
        hora.setValue(null);
    }

}
