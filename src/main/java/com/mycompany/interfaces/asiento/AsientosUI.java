package com.mycompany.interfaces.asiento;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mycompany.components.Navegacion;
import com.mycompany.interfaces.sesion.CrearSesionUI;
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
public class AsientosUI extends UI {

    private static List<String> listadoId = new ArrayList<>();
    private static DBCollection asientos = null;

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

        // tabla de asientos
        final Table tablaAsientos = obtenerTabla();

        // panel de edición
        final Panel panelEdit = new Panel("Gestión del asiento");
        final VerticalLayout vLayout = new VerticalLayout();
        final Label info = new Label("Para facilitar la edición, puedes seleccionar"
                + " un registro de la tabla y luego editarlo.");

        // formulario edición
        final FormLayout form = new FormLayout();

        final ComboBox id = new ComboBox("Asiento", listadoId);
        id.setRequired(true);
        id.setInputPrompt("Selecciona el asiento");
        final ComboBox tipo = new ComboBox("Tipo de asiento", comboTipos());
        tipo.setInputPrompt("Selecciona tipo");
        final ComboBox sala = new ComboBox("Sala", comboSalas());
        sala.setInputPrompt("Selecciona la sala");
        final ComboBox disponible = new ComboBox("Disponible", comboDisponible());
        disponible.setInputPrompt("Disponibilidad");
        final Button btnEditar = new Button("Modificar");
        btnEditar.setStyleName("primary");
        final Button btnEliminar = new Button("Eliminar");
        btnEliminar.setStyleName("danger");

        final HorizontalLayout hLayout = new HorizontalLayout();
        hLayout.addComponents(btnEditar, btnEliminar);
        hLayout.setSpacing(true);

        form.addComponents(id, tipo, sala, disponible, hLayout);
        form.setMargin(true);

        vLayout.addComponents(info, form);
        vLayout.setMargin(true);
        panelEdit.setContent(vLayout);

        // Estructura del grid
        grid.addComponent(tablaAsientos, 0, 0);
        grid.addComponent(panelEdit, 1, 0);
        grid.setSizeFull();
        grid.setSpacing(true);

        // si selecciona un registro de la tabla
        // se añaden los datos al formulario
        tablaAsientos.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                final DBCursor cursor = asientos.find();

                DBObject asiento = null;
                while (cursor.hasNext()) {
                    asiento = cursor.next();
                    if (asiento.get("_id").equals(event.getItemId())) {
                        id.setValue(asiento.get("_id"));
                        tipo.setValue(asiento.get("tipo"));
                        sala.setValue(asiento.get("sala"));
                        disponible.setValue(asiento.get("disponible").toString());
                        break;
                    }
                }
            }
        });

        // al pulsar el botón de editar
        btnEditar.addClickListener(e -> {
            if (Objects.nonNull(id.getValue()) && (Objects.nonNull(tipo.getValue()))) {
                BasicDBObject asiento = new BasicDBObject();
                if (tipo.getValue() != null) {
                    asiento.append("tipo", tipo.getValue());
                }
                if (sala.getValue() != null) {
                    asiento.append("sala", sala.getValue());
                }
                if (disponible.getValue() != null) {
                    asiento.append("disponible", disponible.getValue());
                }

                // asiento a actualizar
                BasicDBObject asientoUpdate = new BasicDBObject();
                asientoUpdate.put("$set", asiento);
                // buscar por id
                BasicDBObject buscarPorId = new BasicDBObject();
                buscarPorId.append("_id", id.getValue());
                // actualiza el elemento por id
                asientos.update(buscarPorId, asientoUpdate);
                Notification.show("Los datos se han modificado correctamente.", Notification.Type.TRAY_NOTIFICATION);
                // limpiar campos
                resetarCampos(id, tipo, sala, disponible);
                // actualizamos la tabla
                actualizarTabla(tablaAsientos);
            } else if (Objects.nonNull(id.getValue())) {
                Notification.show("Debes rellenar algún campo más.", Notification.Type.ERROR_MESSAGE);
            } else {
                Notification.show("El campo 'Asiento' es obligatorio.", Notification.Type.ERROR_MESSAGE);
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
            if (Objects.nonNull(id.getValue())) {
                addWindow(ventanaConfirmacion);
            } else {
                Notification.show("Primero debes de seleccionar un asiento", Notification.Type.ERROR_MESSAGE);
            }
        });

        // al pulsar el botón de confirmar eliminación
        btnConfirmar.addClickListener(e -> {
            // Obtengo el asiento
            DBObject asiento = asientos.findOne(new BasicDBObject().append("_id", id.getValue()));

            if (Objects.nonNull(asiento)) {
                // Elimino el asiento
                asientos.remove(asiento);

                // actualizo tabla y elimino ventana
                actualizarTabla(tablaAsientos);
                removeWindow(ventanaConfirmacion);
                // resetea los campos
                resetarCampos(id, tipo, sala, disponible);

                Notification.show("El registro se ha eliminado correctamente", Notification.Type.TRAY_NOTIFICATION);
            }
        });

        // al pulsar el botón de cancelar
        btnCancelar.addClickListener(e -> {
            removeWindow(ventanaConfirmacion);
        });

        // ESTRUCTURA DE LA INTERFAZ
        rootLayout.addComponents(btnLogout, navbar, grid);

        rootLayout.setMargin(true);
        rootLayout.setSpacing(true);

        setContent(rootLayout);
    }

    @WebServlet(urlPatterns = "/asientos/*", name = "AsientosUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = AsientosUI.class, productionMode = false)
    public static class AsientosUIServlet extends VaadinServlet {
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
     * Método encargado de obtener la lista de asientos general, crear una tabla
     * con ella y devolverla
     *
     * @return Tabla de asientos
     */
    private static Table obtenerTabla() {
        final Table tabla = new Table();
        tabla.addContainerProperty("Asiento", String.class, null);
        tabla.addContainerProperty("Tipo", String.class, null);
        tabla.addContainerProperty("Sala", String.class, null);
        tabla.addContainerProperty("Disponible", String.class, null);
        
        BBDD bbdd = null;
        try {
            bbdd = new BBDD("asientos");
        } catch (UnknownHostException ex) {
            Logger.getLogger(AsientosUI.class.getName()).log(Level.SEVERE, null, ex);
        }

        asientos = bbdd.getColeccion();
        final DBCursor cursor = asientos.find();

        DBObject asiento = null;
        while (cursor.hasNext()) {
            asiento = cursor.next();
            String id = asiento.get("_id").toString();
            String tipo = asiento.get("tipo").toString();
            String sala = asiento.get("sala").toString();
            String disponible = asiento.get("disponible").toString();
            tabla.addItem(new Object[]{id, tipo, sala, disponible.equals("Si") ? "Si" : "No"}, id);
            listadoId.add(id);
        }

        tabla.setSelectable(true);
        tabla.setSizeFull();
        Object[] properties = {"Sala"};
        boolean[] ordering = {true};
        tabla.sort(properties, ordering);
        return tabla;
    }

    /**
     * Método encargado de actualizar la tabla de asientos
     *
     * @param tabla Tabla de asientos
     */
    private static void actualizarTabla(Table tabla) {
        tabla.removeAllItems();
        listadoId.clear();
        final DBCursor cursor = asientos.find();

        DBObject asiento = null;
        while (cursor.hasNext()) {
            asiento = cursor.next();
            String id = asiento.get("_id").toString();
            String tipo = asiento.get("tipo").toString();
            String sala = asiento.get("sala").toString();
            String disponible = asiento.get("disponible").toString();
            tabla.addItem(new Object[]{id, tipo, sala, disponible.equals("Si") ? "Si" : "No"}, id);
            listadoId.add(id);
        }

        Object[] properties = {"Sala"};
        boolean[] ordering = {true};
        tabla.sort(properties, ordering);
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
     * Método encargado de devolver la lista de salas existentes
     *
     * @return Listado de salas en bbdd
     */
    private static List<String> comboSalas() {
        final List<String> salas = new ArrayList<>();
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
            salas.add(sala.get("_id").toString());
        }
        
        salas.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        return salas;
    }
    
    /**
     * Método encargado de cargar el combo de opciones de disponibilidad
     *
     * @return Listado de tipos de opciones
     */
    private static List<String> comboDisponible() {
        List<String> opciones = new ArrayList<>();
        opciones.add("Si");
        opciones.add("No");
        return opciones;
    }

    /**
     * Método encargado de resetear los campos del formulario
     *
     * @param id Identificador del asiento
     * @param tipo Tipo de asiento
     * @param sala Sala del asiento
     * @param disponible Disponibilidad del asiento
     */
    private static void resetarCampos(ComboBox id, ComboBox tipo, ComboBox sala, ComboBox disponible) {
        id.setValue(null);
        tipo.setValue(null);
        sala.setValue(null);
        disponible.setValue(null);
    }

}
