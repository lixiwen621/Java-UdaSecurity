module com.udacity.image {
    requires java.desktop;
    requires org.slf4j;
    requires software.amazon.awssdk.services.rekognition;
    requires com.google.guice;
    requires software.amazon.awssdk.core;
    requires com.udacity.constant;
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.regions;
    requires com.google.gson;
    exports com.udacity.image.service;

}