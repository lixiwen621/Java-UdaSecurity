module com.udacity.security {
    requires com.google.common;
    requires com.google.gson;
    requires com.udacity.image;
    requires com.google.guice;
    requires com.udacity.constant;
    requires java.desktop;
    requires org.slf4j;
    requires java.prefs;
    requires ch.qos.logback.core;
    exports com.udacity.security.service;
    exports com.udacity.security.model;
    exports com.udacity.security.data;
    opens com.udacity.security.model;
    opens com.udacity.security.data;
    opens com.udacity.security.service to com.google.guice;

}