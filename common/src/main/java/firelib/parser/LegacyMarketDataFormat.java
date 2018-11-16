package firelib.parser;

import java.io.FileInputStream;
import java.util.Properties;


/*
to handle files like below :
[CHAIN]
PREVFILE=\201212\
[FORMATS]
DATEFORMAT=DD.MM.YYYY
TIMEFORMAT=HHMMSS
COLUMNFORMAT=D_T_#_P_V_U_B_A_I
TIMEZONE=NY
     */

public class  LegacyMarketDataFormat {
    public String[] COLUMNFORMAT; // list of tokens: 'D', 'T', '#', 'P', 'V', 'I'.
    public String DATEFORMAT;
    public String NEXTFILE;
    public String PREVFILE;
    public String TIMEFORMAT;
    public String TIMEZONE;


    public LegacyMarketDataFormat loadFromFile(String fn) {

        try (FileInputStream inStream = new FileInputStream(fn)) {
            Properties properties = new Properties();
            properties.load(inStream);
            DATEFORMAT = properties.getProperty("DATEFORMAT");
            TIMEFORMAT = properties.getProperty("TIMEFORMAT");
            TIMEZONE = properties.getProperty("TIMEZONE");
            NEXTFILE = properties.getProperty("NEXTFILE");
            PREVFILE = properties.getProperty("PREVFILE");
            COLUMNFORMAT = properties.getProperty("COLUMNFORMAT").split("_");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }
}


