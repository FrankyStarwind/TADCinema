package com.mycompany.interfaces;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.ClassResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.WrappedSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;

@Theme("mytheme")
@PreserveOnRefresh
public class SalaUI extends UI {

    public static WrappedSession session = null; //Definimos el elemento de sesión

    @Override
    protected void init(VaadinRequest request) {
        final VerticalLayout layout = new VerticalLayout();

        final Table tablePeliculas = new Table();
        definirCabeceraTabla(tablePeliculas);
        
        List<Button> botonesCompra= new ArrayList<Button>();
        
        for (int i = 0; i < 10; i++) {
            Button b= new Button("Comprar");
            botonesCompra.add(b);
            tablePeliculas.addItem(new Object[]{i+1,i+1,"Libre",b},i+1 );
        }
        for (int i = 0; i < botonesCompra.size(); i++) {
            botonesCompra.get(i).addClickListener(e->{
                Notification.show("Comprando la entrada!",
                    Notification.Type.HUMANIZED_MESSAGE);
            });
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
        layout.addComponent(new Image("Imagen sala",
                new ClassResource("sala.JPG")));
        layout.addComponents(tablePeliculas);
        layout.setMargin(true);
        layout.setSpacing(true);

        setContent(layout);
    }

    @WebServlet(urlPatterns = "/sala/*", name = "SalaUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = SalaUI.class, productionMode = false)
    public static class SalaUIServlet extends VaadinServlet {
    }

    public void definirCabeceraTabla(Table table) {
        table.addContainerProperty("Fila", Integer.class, null);
        table.addContainerProperty("Asiento", Integer.class, null);
        table.addContainerProperty("Ocupada", String.class, null);
        table.addContainerProperty("Comprar?", Button.class, null);//boton

        //table.setSelectable(true); //Para poder seleccionar los registros
        table.setSizeFull();
    }
}
