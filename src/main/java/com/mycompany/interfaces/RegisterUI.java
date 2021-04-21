package com.mycompany.interfaces;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import javax.servlet.annotation.WebServlet;

@Theme("mytheme")
public class RegisterUI extends UI {

    @Override
    protected void init(VaadinRequest request) {
        // Creación de layouts e información a modo de label
        final VerticalLayout verticalLayout = new VerticalLayout();
        final Label labelInfo = new Label("Bienvenido a TAD Cinema, para poder"
                + " registrarte debes de rellenar todos los campos del formulario");
        final FormLayout form = new FormLayout();

        // Campos requeridos del formulario
        final TextField name = new TextField("Nombre");
        name.setRequired(true);
        final TextField surname = new TextField("Apellidos");
        surname.setRequired(true);
        final TextField dni = new TextField("Dni");
        dni.setRequired(true);
        final TextField password = new TextField("Contraseña");
        password.setRequired(true);

        // Layout simulando un div inline
        final HorizontalLayout divButtons = new HorizontalLayout();
        
        // Botón para registrarse
        final Button btnRegister = new Button("Regístrate");
        btnRegister.setStyleName("primary");
        // Botón para cancelar el registro
        final Button btnCancel = new Button("Cancelar");
        
        divButtons.addComponents(btnRegister, btnCancel);
        divButtons.setSpacing(true);
        
        // redireccion a login al pulsar el boton
        btnCancel.addClickListener(e -> {
            Page.getCurrent().setLocation("/login");
        });
        
        // Se añaden los componentes al formulario
        form.addComponents(name, surname, dni, password, divButtons);

        verticalLayout.addComponents(labelInfo, form);
        verticalLayout.setMargin(true);
        verticalLayout.setSpacing(true);

        setContent(verticalLayout);
    }

    @WebServlet(urlPatterns = "/registro/*", name = "RegisterUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = RegisterUI.class, productionMode = false)
    public static class RegisterUIServlet extends VaadinServlet {
    }

}
