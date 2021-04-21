
package com.mycompany.interfaces;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.WrappedSession;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import javax.servlet.annotation.WebServlet;

@Theme("mytheme")
public class CompraUI extends UI {

    public static WrappedSession session = null; //Definimos el elemento de sesión
    @Override
    protected void init(VaadinRequest request) {
        // Layout general con css
        final VerticalLayout layout = new VerticalLayout();

        // Layouts vertical y formulario
        final VerticalLayout verticalLayout = new VerticalLayout();
        final FormLayout form = new FormLayout();

        final Table tablePeliculas = new Table();
        definirCabeceraTabla(tablePeliculas);
        
        for (int i = 0; i < 10; i++) {
            tablePeliculas.addItem(new Object[]{"Sharknado "+i,"16:00"," 18:00","20:00"},i+1 );
        }

        tablePeliculas.addItemClickListener(
                new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                session = getSession().getSession();
                String nomPeli=event.getItem().getItemProperty("Pelicula").getValue().toString();
                session.setAttribute("sessionNombrePelicula", nomPeli);
                Notification.show("Entrando en las sesiones de "+nomPeli, "Entrando, espere por favor",
                    Notification.Type.HUMANIZED_MESSAGE);
                //Page.getCurrent().setLocation("/"+"session");
            }
        });
        verticalLayout.addComponents(form,tablePeliculas);
        verticalLayout.setMargin(true);
        verticalLayout.setSpacing(true);

        layout.addComponent(verticalLayout);

        setContent(layout);
    }

    @WebServlet(urlPatterns = "/compra/*", name = "CompraUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = CompraUI.class, productionMode = false)
    public static class CompraUIServlet extends VaadinServlet {
    }
    public void definirCabeceraTabla(Table table) {
        table.addContainerProperty("Película", String.class, null);
        table.addContainerProperty("Sesión 1", String.class, null);
        table.addContainerProperty("Sesión 2", String.class, null);
        table.addContainerProperty("Sesión 3", String.class, null);
        
        table.setSelectable(true); //Para poder seleccionar los registros
        table.setSizeFull();
    }
}
