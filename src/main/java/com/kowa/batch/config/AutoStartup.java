package com.kowa.batch.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

@Service
public class AutoStartup {



    @Autowired
    Environment environment;



    @EventListener
    public void openBrowserOnServletContainerInitialized(
            final EmbeddedServletContainerInitializedEvent event) throws IOException {

        final EmbeddedServletContainer embeddedServletContainer = event.getEmbeddedServletContainer();
        final int port;

        if (embeddedServletContainer instanceof TomcatEmbeddedServletContainer) {

            port = ((TomcatEmbeddedServletContainer) embeddedServletContainer)
                    .getTomcat().getConnector().getPort();

        } else {
            port = embeddedServletContainer.getPort(); // Maybe
        }

        String springPort = environment.getProperty("local.server.port");

        String proUrl = "http://localhost:" + port;
        if(Desktop.isDesktopSupported()){
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(URI.create(proUrl));
        }else{
            Runtime rt = Runtime.getRuntime();
            rt.exec( "rundll32" + " url.dll,FileProtocolHandler "+proUrl);
        }

    }
}
