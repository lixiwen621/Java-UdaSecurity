module com.udacity.application {
    requires ch.qos.logback.core;
    requires com.udacity.security;
    requires miglayout.swing;
    requires com.google.common;
    requires com.udacity.image;
    requires com.udacity.constant;
    requires com.google.guice;
    requires java.desktop;
    requires org.slf4j;
    exports com.udacity.application.panel;
    opens com.udacity.application.panel to com.google.guice;
    opens com.udacity.application to com.google.guice;
}