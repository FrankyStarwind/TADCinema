package com.mycompany.interfaces.sala;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mycompany.components.Navegacion;
import com.mycompany.interfaces.pelicula.PeliculasUI;
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
import com.vaadin.ui.TextField;
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
public class SalasUI extends UI {

    private static List<Integer> listadoId = new ArrayList<>();
    private static DBCollection salas = null;

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

        // botón crear sala (redirige a la ui determinada)
        final HorizontalLayout botoneraCrear = new HorizontalLayout();
        final Button btnCrear = new Button("Crear sala");
        botoneraCrear.addComponent(btnCrear);
        // al pulsar el botón de crear
        btnCrear.addClickListener(e -> {
            Page.getCurrent().setLocation("/crear-sala");
        });

        // tabla de salas
        final Table tablaSalas = obtenerTabla();

        // panel de edición
        final Panel panelEdit = new Panel("Gestión de la sala");
        final VerticalLayout vLayout = new VerticalLayout();
        final Label info = new Label("Para facilitar la edición, puedes seleccionar"
                + " un registro de la tabla y luego editarlo.");

        // formulario edición
        final FormLayout form = new FormLayout();

        final ComboBox numero = new ComboBox("Número de sala", listadoId);
        numero.setRequired(true);
        numero.setInputPrompt("Selecciona la sala");
        final TextField capacidad = new TextField("Capacidad");
        capacidad.setInputPrompt("Introduce la capacidad");
        final ComboBox tipo = new ComboBox("Tipo de sala", comboTipos());
        tipo.setInputPrompt("Selecciona tipo");
        final Button btnEditar = new Button("Modificar");
        btnEditar.setStyleName("primary");
        final Button btnEliminar = new Button("Eliminar");
        btnEliminar.setStyleName("danger");

        final HorizontalLayout hLayout = new HorizontalLayout();
        hLayout.addComponents(btnEditar, btnEliminar);
        hLayout.setSpacing(true);

        form.addComponents(numero, capacidad, tipo, hLayout);
        form.setMargin(true);

        vLayout.addComponents(info, form);
        vLayout.setMargin(true);
        panelEdit.setContent(vLayout);

        // Estructura del grid
        grid.addComponent(tablaSalas, 0, 0);
        grid.addComponent(panelEdit, 1, 0);
        grid.setSizeFull();
        grid.setSpacing(true);

        // si selecciona un registro de la tabla
        // se añaden los datos al formulario
        tablaSalas.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                final DBCursor cursor = salas.find();

                DBObject sala = null;
                while (cursor.hasNext()) {
                    sala = cursor.next();
                    if (sala.get("_id").equals(event.getItemId().toString())) {
                        numero.setValue(event.getItemId());
                        capacidad.setValue(sala.get("capacidad").toString());
                        tipo.setValue(sala.get("tipo"));
                        break;
                    }
                }
            }
        });

        // al pulsar el botón de editar
        btnEditar.addClickListener(e -> {
            if (Objects.nonNull(numero.getValue()) && (Objects.nonNull(tipo.getValue()) || !capacidad.getValue().equals(""))) {
                BasicDBObject sala = new BasicDBObject();
                if (!capacidad.getValue().equals("")) {
                    sala.append("capacidad", capacidad.getValue());
                }
                if (tipo.getValue() != null) {
                    sala.append("tipo", tipo.getValue());
                }

                // sala a actualizar
                BasicDBObject salaUpdate = new BasicDBObject();
                salaUpdate.put("$set", sala);
                // buscar por número
                BasicDBObject buscarPorId = new BasicDBObject();
                buscarPorId.append("_id", numero.getValue().toString());
                // actualiza el elemento por número
                salas.update(buscarPorId, salaUpdate);
                Notification.show("Los datos se han modificado correctamente.", Notification.Type.TRAY_NOTIFICATION);
                // limpiar campos
                resetarCampos(numero, capacidad, tipo);
                // actualizamos la tabla
                actualizarTabla(tablaSalas);
            } else if (Objects.nonNull(numero.getValue())) {
                Notification.show("Debes rellenar algún campo más.", Notification.Type.ERROR_MESSAGE);
            } else {
                Notification.show("El campo 'Número de sala' es obligatorio.", Notification.Type.ERROR_MESSAGE);
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
            if (Objects.nonNull(numero.getValue())) {
                addWindow(ventanaConfirmacion);
            } else {
                Notification.show("Primero debes de seleccionar un número de sala", Notification.Type.ERROR_MESSAGE);
            }
        });

        // al pulsar el botón de confirmar eliminación
        btnConfirmar.addClickListener(e -> {
            // Obtengo la sala
            DBObject sala = salas.findOne(new BasicDBObject().append("_id", numero.getValue().toString()));

            if (Objects.nonNull(sala)) {
                // Elimino la sala
                salas.remove(sala);
                
                try {
                    // al eliminar la sala, vamos a eliminar también sus asientos
                    final BBDD bbdd = new BBDD("asientos");
                    final DBCollection asientos = bbdd.getColeccion();
                    
                    final BasicDBObject asiento = new BasicDBObject();
                    asiento.append("sala", sala.get("_id"));
                    final DBCursor cursor = asientos.find(asiento);
                    
                    DBObject a = null;
                    while(cursor.hasNext()) {
                        a = cursor.next();
                        asientos.remove(a);
                    }
                } catch (UnknownHostException ex) {
                    Logger.getLogger(PeliculasUI.class.getName()).log(Level.SEVERE, null, ex);
                }

                // actualizo tabla y elimino ventana
                actualizarTabla(tablaSalas);
                removeWindow(ventanaConfirmacion);
                // reseteo de campos
                resetarCampos(numero, capacidad, tipo);

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

    @WebServlet(urlPatterns = "/salas/*", name = "SalasUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = SalasUI.class, productionMode = false)
    public static class SalasUIServlet extends VaadinServlet {
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
     * Método encargado de obtener la lista de salas, crear una tabla con ella y
     * devolverla
     *
     * @return Tabla de salas
     */
    private static Table obtenerTabla() {
        final Table tabla = new Table();
        tabla.addContainerProperty("Número", Integer.class, null);
        tabla.addContainerProperty("Capacidad", Integer.class, null);
        tabla.addContainerProperty("Tipo", String.class, null);

        BBDD bbdd = null;
        try {
            bbdd = new BBDD("salas");
        } catch (UnknownHostException ex) {
            Logger.getLogger(SalasUI.class.getName()).log(Level.SEVERE, null, ex);
        }

        salas = bbdd.getColeccion();
        final DBCursor cursor = salas.find();

        DBObject sala = null;
        while (cursor.hasNext()) {
            sala = cursor.next();
            Integer numero = Integer.valueOf(sala.get("_id").toString());
            Integer capacidad = Integer.valueOf(sala.get("capacidad").toString());
            String tipo = sala.get("tipo").toString();
            tabla.addItem(new Object[]{numero, capacidad, tipo}, numero);
            listadoId.add(numero);
        }
        
        listadoId.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });

        tabla.setSelectable(true);
        tabla.setSizeFull();
        Object[] properties = {"Número"};
        boolean[] ordering = {true};
        tabla.sort(properties, ordering);
        return tabla;
    }

    /**
     * Método encargado de actualizar la tabla de salas
     *
     * @param tabla Tabla de salas
     */
    private static void actualizarTabla(Table tabla) {
        tabla.removeAllItems();
        listadoId.clear();
        final DBCursor cursor = salas.find();

        DBObject sala = null;
        while (cursor.hasNext()) {
            sala = cursor.next();
            Integer numero = Integer.valueOf(sala.get("_id").toString());
            Integer capacidad = Integer.valueOf(sala.get("capacidad").toString());
            String tipo = sala.get("tipo").toString();
            tabla.addItem(new Object[]{numero, capacidad, tipo}, numero);
            listadoId.add(numero);
        }

        Object[] properties = {"Número"};
        boolean[] ordering = {true};
        tabla.sort(properties, ordering);
    }

    /**
     * Método encargado de cargar el combo de tipo de sala
     *
     * @return Listado de tipos de sala
     */
    private static List<String> comboTipos() {
        List<String> tipos = new ArrayList<>();
        tipos.add("Predeterminada");
        tipos.add("3D");
        tipos.add("iMax");
        return tipos;
    }

    /**
     * Método encargado de resetear los campos del formulario
     *
     * @param numero Número de sala
     * @param capacidad Capacidad máxima de asientos
     * @param tipo Tipo de sala
     */
    private static void resetarCampos(ComboBox numero, TextField capacidad, ComboBox tipo) {
        numero.setValue(null);
        capacidad.setValue("");
        tipo.setValue(null);
    }

}
