package com.udacity.application;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.udacity.application.panel.CatpointGui;
import com.udacity.application.panel.PanelModule;
import com.udacity.constant.common.Constants;
import com.udacity.image.service.ImageModule;
import com.udacity.security.data.SecurityModule;

/**
 * This is the main class that launches the application.
 *  这个是启动应用程序的主类
 */
public class CatpointApp {
    @Inject
    private CatpointGui gui;
    public void run(){
        Guice.createInjector(new SecurityModule(), new ImageModule(Constants.AWS_CONTROL),new PanelModule()).injectMembers(this);
        gui.builder();
        gui.setVisible(true);
    }
    public static void main(String[] args) {
        new CatpointApp().run();
    }
}
