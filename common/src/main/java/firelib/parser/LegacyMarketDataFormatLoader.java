package firelib.parser;

import java.util.ArrayList;
import java.util.List;

public class LegacyMarketDataFormatLoader {

    public static MarketDataFormatMetadata load(String fn){
        LegacyMarketDataFormat settings = new LegacyMarketDataFormat().loadFromFile(fn);
        return transform(settings);
    }

    public static MarketDataFormatMetadata transform(LegacyMarketDataFormat settings) {
        return new MarketDataFormatMetadata(getDateFormat(settings),transformCols(settings.COLUMNFORMAT),getZone(settings));
    }

    private static String getZone(LegacyMarketDataFormat settings){
        switch (settings.TIMEZONE){
            case "NY":
                return "America/New_York";
            case "MOSCOW":
                return "Europe/Moscow";
            case "LONDON":
                return "Europe/London";
            default:
                throw new RuntimeException("not supported legacy time zone " + settings.TIMEZONE);
        }
    }

    private static String[] transformCols(String[] cols){
        List<String> ret = new ArrayList();
        for (int i = 0; i < cols.length; i++) {
            if(cols[i].equals("T")){
                continue;
            }
            if(cols[i].equals("#") && i > 0 && cols[i - 1].equals("T")){
                continue;
            }
            ret.add(cols[i]);
        }
        return ret.toArray(new String[0]);
    }

    private static boolean isMillisSymbol(LegacyMarketDataFormat legacyMarketDataFormat, int i) {
        return legacyMarketDataFormat.COLUMNFORMAT[i].equals("#") && legacyMarketDataFormat.COLUMNFORMAT[i - 1].equals("T") && legacyMarketDataFormat.COLUMNFORMAT[i - 2].equals("D");
    }


    private static String getDateFormat(LegacyMarketDataFormat settings){
        if(settings.DATEFORMAT.equals("DD.MM.YYYY") && settings.TIMEFORMAT.equals("HHMMSS")){

            if(isMillisSymbol(settings,2)){
                return "dd.MM.yyyy,HHmmss.SSS";
            }
            return "dd.MM.yyyy,HHmmss";

        }

        if(settings.DATEFORMAT.equals("YYYY-MM-DD") && settings.TIMEFORMAT.equals("HH:MM:SS")){
            if(isMillisSymbol(settings,2)){
                return "yyyy-MM-dd HH:mm:ss.SSS";
            }
            return "yyyy-MM-dd HH:mm:ss";
        }
        throw new RuntimeException("not supported");
    }
}
