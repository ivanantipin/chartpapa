package firelib.parser;

import java.io.FileInputStream;
import java.util.Properties;

public class MarketDataFormatMetadata{
    String dateFormat;
    String[] columnsMetadata;
    String timeZone;

    public MarketDataFormatMetadata(String dateFormat, String[] columnsMetadata, String timeZone) {
        this.dateFormat = dateFormat;
        this.columnsMetadata = columnsMetadata;
        this.timeZone = timeZone;
    }

    public static MarketDataFormatMetadata loadFromFile(String fn) {
        try (FileInputStream inStream = new FileInputStream(fn)) {
            Properties properties = new Properties();
            properties.load(inStream);
            String dateFormat = properties.getProperty("date.format");
            String timeZone = properties.getProperty("time.zone");
            String[] columnsMetadata = properties.getProperty("column.metadata").split("_");
            return new MarketDataFormatMetadata(dateFormat,columnsMetadata,timeZone);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
