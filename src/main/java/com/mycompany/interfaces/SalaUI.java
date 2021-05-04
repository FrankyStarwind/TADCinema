package com.mycompany.interfaces;

import com.mycompany.components.Navegacion;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.ClassResource;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.WrappedSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;

@Theme("mytheme")
@PreserveOnRefresh
public class SalaUI extends UI {

    public String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();

    @Override
    protected void init(VaadinRequest request) {
        final WrappedSession session = getSession().getSession();
        final VerticalLayout rootLayout = new VerticalLayout();
        final Button btnLogout = new Button("Cerrar sesión");
        
        String s =session.getAttribute("nombrePeli").toString();
        final Label nomPeli = new Label(s);
        
        nomPeli.setCaption(s);
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
                String nomPeli=event.getItem().getItemProperty("Pelicula").getValue().toString();
                session.setAttribute("sessionNombrePelicula", nomPeli);
                Notification.show("Entrando en las sesiones de "+nomPeli, "Entrando, espere por favor",
                    Notification.Type.HUMANIZED_MESSAGE);
                //Page.getCurrent().setLocation("/"+"session");
            }
            
        });
        
        final Image image = new Image(null, new FileResource(new File(basepath + "/WEB-INF/images/sala.JPG")));
        
        rootLayout.addComponents(btnLogout, navbar, nomPeli,image, tablePeliculas);
        rootLayout.setMargin(true);
        rootLayout.setSpacing(true);

        setContent(rootLayout);
    }

    @WebServlet(urlPatterns = "/session/*", name = "SalaUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = SalaUI.class, productionMode = false)
    public static class SalaUIServlet extends VaadinServlet {
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
        table.addContainerProperty("Fila", Integer.class, null);
        table.addContainerProperty("Asiento", Integer.class, null);
        table.addContainerProperty("Ocupada", String.class, null);
        table.addContainerProperty("Comprar?", Button.class, null);//boton

        //table.setSelectable(true); //Para poder seleccionar los registros
        table.setSizeFull();
    }
    
}
