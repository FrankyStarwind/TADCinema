package com.mycompany.interfaces;

import com.vaadin.annotations.StyleSheet;
import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.FormLayout;
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
        
        // Layouts vertical y formulario
        final VerticalLayout verticalLayout = new VerticalLayout();
        final FormLayout form = new FormLayout();
        
        final TextField username = new TextField("Usuario");
        final TextField password = new TextField("Contraseña");
        final Button btnLogin = new Button("Iniciar sesión");
        btnLogin.setStyleName("primary");
        final Button btnRegister = new Button("Regístrate");
        form.addComponents(username, password, btnLogin);
        form.setStyleName(CLASSNAME + "-form");
        
        verticalLayout.addComponents(form, btnRegister);
        verticalLayout.setMargin(true);
        verticalLayout.setSpacing(true);
        
        layout.addComponent(verticalLayout);
        
        setContent(layout);
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = LoginUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
