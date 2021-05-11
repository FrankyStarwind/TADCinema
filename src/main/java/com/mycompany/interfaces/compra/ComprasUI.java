package com.mycompany.interfaces.compra;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mycompany.components.Navegacion;
import com.mycompany.interfaces.sesion.SesionesUI;
import com.mycompany.utils.BBDD;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.WrappedSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import org.bson.types.ObjectId;

@Theme("mytheme")
public class ComprasUI extends UI {

//    private final static List<String> listadoCompras = new ArrayList<>();
    private static DBCollection compras = null;
    private static Object idSelected = null;

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

        // tabla de compras
        final Table tablaCompras = obtenerTabla();

        final Button btnEliminar = new Button("Eliminar");
        btnEliminar.setStyleName("danger");
        final Button btnEliminarTodos = new Button("Eliminar todo");
        btnEliminarTodos.setStyleName("dark");

        // se selecciona un registro de la tabla
        tablaCompras.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                idSelected = event.getItemId();
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

        // botonera de confirmar/cancelar
        final HorizontalLayout botoneraPopup2 = new HorizontalLayout();
        final Button btnConfirmar2 = new Button("Eliminar");
        btnConfirmar2.setStyleName("danger");
        final Button btnCancelar2 = new Button("Cancelar");
        botoneraPopup2.addComponents(btnConfirmar2, btnCancelar2);
        botoneraPopup2.setMargin(true);
        botoneraPopup2.setSpacing(true);

        // ventana confirmación
        final Window ventanaConfirmacion2 = new Window("¿Estás seguro?");
        ventanaConfirmacion2.center();
        ventanaConfirmacion2.setClosable(false);
        ventanaConfirmacion2.setDraggable(false);
        ventanaConfirmacion2.setResizable(false);
        ventanaConfirmacion2.setContent(botoneraPopup2);

        // al pulsar el botón de eliminar
        btnEliminar.addClickListener(e -> {
            if (Objects.nonNull(idSelected)) {
                addWindow(ventanaConfirmacion);
            } else {
                Notification.show("Primero debes de seleccionar un registro", Notification.Type.ERROR_MESSAGE);
            }
        });

        // al pulsar el botón de confirmar eliminación
        btnConfirmar.addClickListener(e -> {
            // Obtengo la compra
            DBObject compra = compras.findOne(new BasicDBObject().append("_id", idSelected));

            if (Objects.nonNull(compra)) {
                // Elimino la conmpra
                compras.remove(compra);

                try {
                    final BBDD bbdd = new BBDD("ocupados");
                    final DBCollection ocupados = bbdd.getColeccion();
                    final BasicDBObject ocupado = new BasicDBObject();
                    ocupado.append("hora", compra.get("horaSesion"));
                    ocupado.append("sala", compra.get("sala"));
                    ocupado.append("fila", compra.get("fila"));
                    ocupado.append("numero", compra.get("asiento"));

                    DBObject elemento = ocupados.findOne(ocupado);
                    if (Objects.nonNull(elemento)) {
                        ocupados.remove(elemento);
                    }
                } catch (UnknownHostException ex) {
                    Logger.getLogger(ComprasUI.class.getName()).log(Level.SEVERE, null, ex);
                }

                // actualizo tabla y elimino ventana
                actualizarTabla(tablaCompras);
                removeWindow(ventanaConfirmacion);

                Notification.show("El registro se ha eliminado correctamente", Notification.Type.TRAY_NOTIFICATION);
            }
        });

        // al pulsar el botón de cancelar
        btnCancelar.addClickListener(e -> {
            removeWindow(ventanaConfirmacion);
        });

        // al pulsar el botón de eliminar todos
        btnEliminarTodos.addClickListener(e -> {
            addWindow(ventanaConfirmacion2);
        });

        // al pulsar el botón de confirmar eliminación
        btnConfirmar2.addClickListener(e -> {
            try {
                BBDD bbdd = new BBDD("ocupados");
                final DBCollection ocupados = bbdd.getColeccion();
                DBCursor cursor = ocupados.find();
                
                DBObject elemento = null;
                while(cursor.hasNext()) {
                    elemento = cursor.next();
                    ocupados.remove(elemento);
                }
                
                bbdd = new BBDD("compras");
                final DBCollection compras = bbdd.getColeccion();
                cursor = compras.find();
                
                elemento = null;
                while(cursor.hasNext()) {
                    elemento = cursor.next();
                    compras.remove(elemento);
                }
            } catch (UnknownHostException ex) {
                Logger.getLogger(ComprasUI.class.getName()).log(Level.SEVERE, null, ex);
            }

            // actualizo tabla y elimino ventana
            actualizarTabla(tablaCompras);
            removeWindow(ventanaConfirmacion2);

            Notification.show("Los registros se han eliminado correctamente", Notification.Type.TRAY_NOTIFICATION);
        });

        // al pulsar el botón de cancelar
        btnCancelar2.addClickListener(e -> {
            removeWindow(ventanaConfirmacion2);
        });

        final HorizontalLayout botonera = new HorizontalLayout();
        botonera.setSpacing(true);
        botonera.addComponents(btnEliminar, btnEliminarTodos);

        // ESTRUCTURA DE LA INTERFAZ
        rootLayout.addComponents(btnLogout, navbar, tablaCompras, botonera);

        rootLayout.setMargin(true);
        rootLayout.setSpacing(true);

        setContent(rootLayout);
    }

    @WebServlet(urlPatterns = "/compras/*", name = "ComprasUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = ComprasUI.class, productionMode = false)
    public static class ComprasUIServlet extends VaadinServlet {
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
     * Método encargado de obtener la lista de compras general, crear una tabla
     * con ella y devolverla
     *
     * @return Tabla de compras
     */
    private static Table obtenerTabla() {
        final Table tabla = new Table();
        tabla.addContainerProperty("Nº referencia", String.class, null);
        tabla.addContainerProperty("Usuario", String.class, null);
        tabla.addContainerProperty("Película", String.class, null);
        tabla.addContainerProperty("Fila", Integer.class, null);
        tabla.addContainerProperty("Asiento", Integer.class, null);
        tabla.addContainerProperty("Hora", String.class, null);
        tabla.addContainerProperty("Sala", String.class, null);
        tabla.addContainerProperty("Precio", Double.class, null);

        BBDD bbdd = null;
        try {
            bbdd = new BBDD("compras");
        } catch (UnknownHostException ex) {
            Logger.getLogger(SesionesUI.class.getName()).log(Level.SEVERE, null, ex);
        }

        compras = bbdd.getColeccion();
        final DBCursor cursor = compras.find();

        DBObject compra = null;
        while (cursor.hasNext()) {
            compra = cursor.next();
            ObjectId id = (ObjectId) compra.get("_id");
            String usuario = compra.get("usuario").toString();
            String pelicula = compra.get("nombrePelicula").toString();
            Integer fila = Integer.valueOf(compra.get("fila").toString());
            Integer asiento = Integer.valueOf(compra.get("asiento").toString());
            String hora = compra.get("horaSesion").toString();
            String sala = compra.get("sala").toString();
            Double precio = Double.valueOf(compra.get("precio").toString());
            tabla.addItem(new Object[]{id.toString(), usuario, pelicula, fila, asiento, hora, sala, precio}, id);
//            listadoPeliculas.add(pelicula);
        }

        tabla.setSelectable(true);
        tabla.setSizeFull();
        return tabla;
    }

    /**
     * Método encargado de actualizar la tabla de compras
     *
     * @param tabla Tabla de compras
     */
    private static void actualizarTabla(Table tabla) {
        tabla.removeAllItems();
//        listadoPeliculas.clear();
        final DBCursor cursor = compras.find();

        DBObject compra = null;
        while (cursor.hasNext()) {
            compra = cursor.next();
            ObjectId id = (ObjectId) compra.get("_id");
            String usuario = compra.get("usuario").toString();
            String pelicula = compra.get("nombrePelicula").toString();
            Integer fila = Integer.valueOf(compra.get("fila").toString());
            Integer asiento = Integer.valueOf(compra.get("asiento").toString());
            String hora = compra.get("horaSesion").toString();
            String sala = compra.get("sala").toString();
            Double precio = Double.valueOf(compra.get("precio").toString());
            tabla.addItem(new Object[]{id.toString(), usuario, pelicula, fila, asiento, hora, sala, precio}, id); 
        }
    }

}
