
package com.mycompany.interfaces;

import com.mycompany.components.Navegacion;
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
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import javax.servlet.annotation.WebServlet;

@Theme("mytheme")
public class CompraUI extends UI {

    public static WrappedSession session = null; //Definimos el elemento de sesión
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

        final Table tablePeliculas = new Table();
        definirCabeceraTabla(tablePeliculas);
        
        for (int i = 0; i < 10; i++) {
            tablePeliculas.addItem(new Object[]{"Sharknado "+i,"16:00"," 18:00","20:00"},i+1 );
        }

        tablePeliculas.addItemClickListener(
                new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                String nomPeli=event.getItem().getItemProperty("Película").getValue().toString();
                session.setAttribute("sessionNombrePelicula", nomPeli);
                Notification.show("Entrando en las sesiones de "+nomPeli, "Entrando, espere por favor",
                    Notification.Type.HUMANIZED_MESSAGE);
                //Page.getCurrent().setLocation("/"+"session");
            }
        });
        rootLayout.addComponents(btnLogout, navbar, tablePeliculas);
        rootLayout.setMargin(true);
        rootLayout.setSpacing(true);

        setContent(rootLayout);
    }

    @WebServlet(urlPatterns = "/compra/*", name = "CompraUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = CompraUI.class, productionMode = false)
    public static class CompraUIServlet extends VaadinServlet {
    }
    
    /**
     * Método encargado de comprobar si la sesión existe o no
     * Si no existe, redirecciona al login
     */
    private static void comprobarSesion(final VerticalLayout rootLayout, final WrappedSession session) {
        if(session.getAttribute("usuario") == null){
            Page.getCurrent().setLocation("/");
        } else {
            final Label bienvenido = new Label("Bienvenido, " + session.getAttribute("usuario"));
            rootLayout.addComponent(bienvenido);
        }
    }
    
    public void definirCabeceraTabla(Table table) {
        table.addContainerProperty("Pelicula", String.class, null);
        table.addContainerProperty("Sesión 1", String.class, null);
        table.addContainerProperty("Sesión 2", String.class, null);
        table.addContainerProperty("Sesión 3", String.class, null);
        
        table.setSelectable(true); //Para poder seleccionar los registros
        table.setSizeFull();
    }
    
}
