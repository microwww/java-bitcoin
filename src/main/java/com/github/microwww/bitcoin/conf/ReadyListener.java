package com.github.microwww.bitcoin.conf;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Component
public class ReadyListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Config conf = event.getApplicationContext().getBean(Config.class);
        File path = new File(conf.getBitcoin().getDataDir());
        try {
            path.mkdirs();
            if (!path.canWrite()) {
                throw new RuntimeException("Not to writer dir : " + path.getCanonicalPath());
            }
            File lock = new File(path, "lock");
            lock.createNewFile();
            new FileInputStream(lock).getChannel().lock();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
