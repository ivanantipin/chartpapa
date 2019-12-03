package firelib.parser;

import firelib.domain.Ohlc;
import firelib.domain.Tick;
import javolution.text.TypeFormat;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ParserHandlersProducer {

    private final MarketDataFormatMetadata settings;

    public boolean isOhlc(){
        return new ArrayList<>(Arrays.asList(settings.columnsMetadata)).contains("O");
    }


    public final ZoneId zoneId;
    public final ParseHandler[] handlers;

    private final String dateformat;


    public ParserHandlersProducer(MarketDataFormatMetadata settings){
        this.settings = settings;
        zoneId = ZoneId.of(settings.timeZone);
        dateformat = settings.dateFormat;
        handlers = parsePattern();
    }


    private ParseHandler[] parsePattern()
    {
        List<ParseHandler> handlers = new ArrayList<>();

        for (int i = 0; i < settings.columnsMetadata.length; i++)
        {
            String token = settings.columnsMetadata[i];

            switch (token)
            {
                case "D":
                    if(isOhlc()){
                        handlers.add(new DateTimeHandler<Ohlc>((oh, dt) -> oh.setEndTime(dt), zoneId, dateformat));
                    }else{
                        handlers.add(new DateTimeHandler<Tick>((oh, dt) -> oh.setDtGmt(dt), zoneId, dateformat));
                    }
                    break;
                case "#":
                    handlers.add(new SkipHandler());
                    break;
                case "P":
                    handlers.add(new StdHandler<Tick, Double>((oh, v) -> oh.setLast(v), (chs) -> TypeFormat.parseDouble(chs)));
                    break;
                case "O":
                    handlers.add(new StdHandler<Ohlc, Double>((oh, v) -> oh.setOpen(v), (chs) -> TypeFormat.parseDouble(chs)));
                    break;
                case "H":
                    handlers.add(new StdHandler<Ohlc, Double>((oh, v) -> oh.setHigh(v), (chs) -> TypeFormat.parseDouble(chs)));
                    break;
                case "L":
                    handlers.add(new StdHandler<Ohlc, Double>((oh, v) -> oh.setLow(v), (chs) -> TypeFormat.parseDouble(chs)));
                    break;

                case "C":
                    handlers.add(new StdHandler<Ohlc, Double>((oh, v) -> oh.setClose(v), (chs) -> TypeFormat.parseDouble(chs)));
                    break;

                case "V":
                    if(isOhlc()){
                        handlers.add(new StdHandler<Ohlc, Integer>((oh, v) -> oh.setVolume(v), (chs) -> TypeFormat.parseInt(chs)));
                    }else{
                        handlers.add(new StdHandler<Tick, Integer>((oh, v) -> oh.setVol(v), (chs) -> (int) TypeFormat.parseDouble(chs)));
                    }

                    break;
                case "I":
                    handlers.add(new StdHandler<Tick, Integer>((oh, v) -> oh.setTickNumber(v), (chs) -> TypeFormat.parseInt(chs)));
                    break;
                case "U":
                    //cum volume
                    handlers.add(new SkipHandler());
                    break;
                case "B":
                    handlers.add(new StdHandler<Tick, Double>((oh, v) -> oh.setBid(v), (chs) -> TypeFormat.parseDouble(chs)));
                    break;

                case "A":
                    handlers.add(new StdHandler<Tick, Double>((oh, v) -> oh.setAsk(v), (chs) -> TypeFormat.parseDouble(chs)));
                    break;
                default:
                    throw new RuntimeException("Error: unsupported COLUMNFORMAT token '" + token + "'.");
            }
        }
        List<ParseHandler> withIncs = new ArrayList<>();
        for(int i = 0; i < handlers.size(); i++){
            withIncs.add(handlers.get(i));
            if(i != handlers.size() -1){
                withIncs.add(new IncHandler<>());
            }
        }
        return withIncs.toArray(new ParseHandler[0]);
    }

}
