package com.mycompany.interfaces.pelicula;

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
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;

@Theme("mytheme")
public class PeliculasUI extends UI {

    private static List<String> listadoPeliculas = new ArrayList<>();
    private static DBCollection peliculas = null;
    private static Object idSelect = null;

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

        // botón crear asiento (redirige a la ui determinada)
        final HorizontalLayout botoneraCrear = new HorizontalLayout();
        final Button btnCrear = new Button("Crear Pelicula");
        botoneraCrear.addComponent(btnCrear);
        // al pulsar el botón de crear
        btnCrear.addClickListener(e -> {
            Page.getCurrent().setLocation("/crear-pelicula");
        });

        // tabla de peliculas
        final Table tablaPelis = obtenerTabla();

        // panel de edición
        final Panel panelEdit = new Panel("Gestión de la pelicula");
        final VerticalLayout vLayout = new VerticalLayout();
        final Label info = new Label("Para facilitar la edición, puedes seleccionar"
                + " un registro de la tabla y luego editarlo.");

        // formulario edición
        final FormLayout form = new FormLayout();

        final ComboBox titulo = new ComboBox("Título", listadoPeliculas);
        titulo.setRequired(true);
        titulo.setInputPrompt("Selecciona la pelicula");
        final ComboBox idioma = new ComboBox("Idioma", comboIdiomas());
        idioma.setInputPrompt("Selecciona opcion de idioma");
        final TextField director = new TextField("Director");
        director.setInputPrompt("Introduce el director");
        final TextField anyo = new TextField("Año");
        anyo.setInputPrompt("Introduce el año de estreno");
        final TextField duracion = new TextField("Duración (minutos)");
        duracion.setInputPrompt("Introduce los minutos");
        final Button btnEditar = new Button("Modificar");
        btnEditar.setStyleName("primary");
        final Button btnEliminar = new Button("Eliminar");
        btnEliminar.setStyleName("danger");
        
        final HorizontalLayout hLayout = new HorizontalLayout();
        hLayout.addComponents(btnEditar, btnEliminar);
        hLayout.setSpacing(true);

        form.addComponents(titulo, idioma, director, anyo, duracion, hLayout);
        form.setMargin(true);

        vLayout.addComponents(info, form);
        vLayout.setMargin(true);
        panelEdit.setContent(vLayout);

        // Estructura del grid
        grid.addComponent(tablaPelis, 0, 0);
        grid.addComponent(panelEdit, 1, 0);
        grid.setSizeFull();
        grid.setSpacing(true);

        // si selecciona un registro de la tabla
        // se añaden los datos al formulario
        tablaPelis.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                final DBCursor cursor = peliculas.find();

                DBObject peliculas = null;
                while (cursor.hasNext()) {
                    peliculas = cursor.next();
                    if (peliculas.get("titulo").equals(event.getItemId())) {
                        Object seleccion = peliculas.get("_id");
                        idSelect = seleccion;
                        titulo.setValue(peliculas.get("titulo"));
                        idioma.setValue(peliculas.get("idioma"));
                        director.setValue(peliculas.get("director").toString());
                        anyo.setValue(peliculas.get("año").toString());
                        duracion.setValue(peliculas.get("duracion").toString());

                        break;
                    }
                }
            }
        });

        // al pulsar el botón de editar
        btnEditar.addClickListener(e -> {
            if (Objects.nonNull(titulo.getValue())
                    && (Objects.nonNull(idioma.getValue())
                    || !director.getValue().equals("")
                    || !anyo.getValue().equals("")
                    || !duracion.getValue().equals(""))) {
                BasicDBObject peliEdit = new BasicDBObject();
                if (idioma.getValue() != null) {
                    peliEdit.append("idioma", idioma.getValue());
                }
                if (!director.getValue().equals("")) {
                    peliEdit.append("director", director.getValue());
                }
                if (!anyo.getValue().equals("")) {
                    peliEdit.append("año", anyo.getValue());
                }
                if (!duracion.getValue().equals("")) {
                    peliEdit.append("duracion", duracion.getValue());
                }

                // asiento a actualizar
                BasicDBObject asientoUpdate = new BasicDBObject();
                asientoUpdate.put("$set", peliEdit);
                // buscar por id
                BasicDBObject buscarPorId = new BasicDBObject();
                buscarPorId.append("_id", idSelect);

                // actualiza el elemento por id
                peliculas.update(buscarPorId, asientoUpdate);
                Notification.show("Los datos se han modificado correctamente.", Notification.Type.TRAY_NOTIFICATION);
                // limpiar campos
                resetarCampos(titulo, idioma, director, anyo, duracion);
                // actualizamos la tabla
                actualizarTabla(tablaPelis);
            } else if (Objects.nonNull(titulo.getValue())) {
                Notification.show("Debes rellenar algún campo más.", Notification.Type.ERROR_MESSAGE);
            } else {
                Notification.show("El campo 'Título' es obligatorio.", Notification.Type.ERROR_MESSAGE);
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
            if (Objects.nonNull(titulo.getValue())) {
                addWindow(ventanaConfirmacion);
            } else {
                Notification.show("Primero debes de seleccionar una película", Notification.Type.ERROR_MESSAGE);
            }
        });

        // al pulsar el botón de confirmar eliminación
        btnConfirmar.addClickListener(e -> {
            // Obtengo la película
            DBObject pelicula = peliculas.findOne(new BasicDBObject().append("_id", idSelect));

            if (Objects.nonNull(pelicula)) {
                // Elimino la película
                peliculas.remove(pelicula);

                // actualizo tabla y elimino ventana
                actualizarTabla(tablaPelis);
                removeWindow(ventanaConfirmacion);
                // resetea los campos
                resetarCampos(titulo, idioma, director, anyo, duracion);

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

    @WebServlet(urlPatterns = "/peliculas/*", name = "PeliculasUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = PeliculasUI.class, productionMode = false)
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
     * Método encargado de obtener la lista de peliculas general, crear una
     * tabla con ella y devolverla
     *
     * @return Tabla de peliculas
     */
    private static Table obtenerTabla() {
        final Table tabla = new Table();
        tabla.addContainerProperty("Pelicula", String.class, null);
        tabla.addContainerProperty("Idioma", String.class, null);
        tabla.addContainerProperty("Director", String.class, null);
        tabla.addContainerProperty("Año", Integer.class, null);
        tabla.addContainerProperty("Duración", Integer.class, null);

        BBDD bbdd = null;
        try {
            bbdd = new BBDD("movies");
        } catch (UnknownHostException ex) {
            Logger.getLogger(PeliculasUI.class.getName()).log(Level.SEVERE, null, ex);
        }

        peliculas = bbdd.getColeccion();
        final DBCursor cursor = peliculas.find();

        DBObject pelicula = null;
        while (cursor.hasNext()) {
            pelicula = cursor.next();
            String titulo = pelicula.get("titulo").toString();
            String idioma = pelicula.get("idioma").toString();
            String director = pelicula.get("director").toString();
            Integer anyo = Integer.valueOf(pelicula.get("año").toString());

            Integer duracion = Integer.valueOf(pelicula.get("duracion").toString());
            tabla.addItem(new Object[]{titulo, idioma, director, anyo, duracion}, titulo);
            listadoPeliculas.add(titulo);
        }

        tabla.setSelectable(true);
        tabla.setSizeFull();
        Object[] properties = {"Año"};
        boolean[] ordering = {true};
        tabla.sort(properties, ordering);
        return tabla;
    }

    /**
     * Método encargado de actualizar la tabla de peliculas
     *
     * @param tabla Tabla de peliculas
     */
    private static void actualizarTabla(Table tabla) {
        tabla.removeAllItems();
        listadoPeliculas.clear();
        final DBCursor cursor = peliculas.find();

        DBObject pelicula = null;
        while (cursor.hasNext()) {
            pelicula = cursor.next();
            String titulo = pelicula.get("titulo").toString();
            String idioma = pelicula.get("idioma").toString();
            String director = pelicula.get("director").toString();
            Integer anyo = Integer.valueOf(pelicula.get("año").toString());
            Integer duracion = Integer.valueOf(pelicula.get("duracion").toString());
            tabla.addItem(new Object[]{titulo, idioma, director, anyo, duracion}, titulo);
            listadoPeliculas.add(titulo);
        }

        Object[] properties = {"Año"};
        boolean[] ordering = {true};
        tabla.sort(properties, ordering);
    }

    /**
     * Método encargado de cargar el combo de idiomas
     *
     * @return Listado de idiomas
     */
    private static List<String> comboIdiomas() {
        List<String> tipos = new ArrayList<>();
        tipos.add("Castellano");
        tipos.add("VOSE");
        tipos.add("Latino");
        return tipos;
    }

    /**
     * Método encargado de resetear los campos del formulario
     *
     * @param titulo Título de la película
     * @param idioma Idioma de la película
     * @param director Director de la película
     * @param anyo Año de la película
     * @param duracion Duración en minutos de la película
     */
    private static void resetarCampos(ComboBox titulo, ComboBox idioma, TextField director, TextField anyo, TextField duracion) {
        titulo.setValue(null);
        idioma.setValue(null);
        director.setValue("");
        anyo.setValue("");
        duracion.setValue("");
    }

}
