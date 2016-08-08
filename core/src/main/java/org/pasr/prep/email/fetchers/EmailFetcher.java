package org.pasr.prep.email.fetchers;


import javax.mail.MessagingException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Observable;
import java.util.Properties;
import java.util.logging.Logger;

import static org.pasr.utilities.Utilities.getResourceStream;


public abstract class EmailFetcher extends Observable implements Runnable {
    EmailFetcher(String propertiesResourcePath) throws IOException {
        properties_ = new Properties();

        InputStream inputStream = getResourceStream(propertiesResourcePath);

        if(inputStream == null){
            throw new IOException(
                "getResourceStream(" + propertiesResourcePath + ") returned null"
            );
        }

        properties_.load(inputStream);
    }

    public abstract void open (String address, String password) throws MessagingException;

    public abstract void fetch ();

    public abstract void terminate ();

    Logger getLogger(){
        return logger_;
    }

    Properties properties_;

    private Logger logger_ = Logger.getLogger(getClass().getName());

}
