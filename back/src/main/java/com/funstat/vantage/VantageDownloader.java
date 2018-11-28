package com.funstat.vantage;

import com.funstat.domain.Ohlc;
import com.funstat.domain.InstrId;
import com.funstat.store.MdDao;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class VantageDownloader implements Source{

    public static final String SOURCE = "VANTAGE";
    public static final String MICEX = "MICEX";
    static DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    MdDao mdDao;


    public static void main(String[] args) {

        List<Ohlc> load = new VantageDownloader().load(new InstrId("RASP.MOS", "RASP.MOS", "RASP.MOS", "RASP.MOS", "RASP.MOS"));
        System.out.println(load);

    }

    public static Optional<Ohlc> parse(String str) {
        try {
            String[] arr = str.split(",");
            return Optional.of(new Ohlc(LocalDate.parse(arr[0], pattern).atStartOfDay(),
                    Double.parseDouble(arr[1]),
                    Double.parseDouble(arr[2]), Double.parseDouble(arr[3]), Double.parseDouble(arr[4])
            ));
        }catch (Exception e){
            System.out.println("not valid entry "+ str + " because " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<InstrId> symbols() {
        /*
        fixme
                List<InstrId> ret = mdDao.readGeneric("vantage_symbols", InstrId.class);
        ret.add(new InstrId("RASP.MOS","RASP.MOS", MICEX,"RASP.MOS", SOURCE));
        return ret;

         */
        return new ArrayList<>();
    }

    @Override
    public List<Ohlc> load(InstrId instrId) {
        RestTemplate template = new RestTemplate();

        String url = "https://www.alphavantage.co/query";
        String function = "TIME_SERIES_DAILY";
        String apiKey = "P28H4WI1MIPJPGBP";
        String dataType = "csv";


        String request = url +
                "?function=" + function
                + "&instrId=" + instrId.code
                + "&apikey=" + apiKey
                + "&datatype=" + dataType;


        ResponseEntity<String> entity = template.getForEntity(request, String.class);
        return Arrays.asList(entity.getBody().split("\r\n")).stream().map(oh -> {
            return parse(oh);
        }).filter(oh -> oh.isPresent()).map(oh -> oh.get()).collect(Collectors.toList());

    }

    @Override
    public List<Ohlc> load(InstrId instrId, LocalDateTime dateTime) {
        return load(instrId);
    }

    @Override
    public String getName() {
        return SOURCE;
    }


}
