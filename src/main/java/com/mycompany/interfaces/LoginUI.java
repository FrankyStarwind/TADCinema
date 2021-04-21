package com.mycompany.interfaces;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("mytheme")
public class LoginUI extends UI {
    
    // Nombre de clase de la interfaz completa
    public static final String CLASSNAME = "login";

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        // Layout general con css
        final CssLayout layout = new CssLayout();
        layout.setStyleName(CLASSNAME);
        
        // Layouts vertical y formulario y el label de información
        final VerticalLayout verticalLayout = new VerticalLayout();
        final Label labelInfo = new Label("Inicia sesión para entrar a las funcionalidades de la aplicación");
        final FormLayout form = new FormLayout();
        
        // Campos usuario y contraseña
        final TextField username = new TextField("Usuario");
        final TextField password = new TextField("Contraseña");
        
        // Layout simulando un div inline
        final HorizontalLayout divButtons = new HorizontalLayout();
        
        // Botón de inicio de sesión
        final Button btnLogin = new Button("Iniciar sesión");
        btnLogin.setStyleName("primary");
        
        // Botón de registro
        final Button btnRegister = new Button("Regístrate");
        
        divButtons.addComponents(btnLogin, btnRegister);
        divButtons.setSpacing(true);
        
        // redireccion a registro al pulsar el boton
        btnRegister.addClickListener(e -> {
            Page.getCurrent().setLocation("/registro");
        });
        
        // Se añaden los componentes al formulario
        form.addComponents(username, password, divButtons);
        form.setStyleName(CLASSNAME + "-form");
        
        verticalLayout.addComponents(labelInfo, form);
        verticalLayout.setMargin(true);
        verticalLayout.setSpacing(true);
        
        layout.addComponent(verticalLayout);
        
        setContent(layout);
    }

    @WebServlet(urlPatterns = "/*", name = "LoginUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = LoginUI.class, productionMode = false)
    public static class LoginUIServlet extends VaadinServlet {
    }
}
