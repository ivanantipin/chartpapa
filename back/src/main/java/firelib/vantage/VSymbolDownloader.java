package firelib.vantage;

import firelib.core.domain.InstrId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class VSymbolDownloader {

    public static void updateVantageSymbols() {
        String url = "https://www.alphavantage.co/query?function=SYMBOL_SEARCH&keywords=<to_replace>&apikey=P28H4WI1MIPJPGBP&datatype=csv";
        //symbol,name,type,region,marketOpen,marketClose,timezone,currency,matchScore
        for (int i = 97; i <= 'z'; i++) {
            for (int j = 97; j <= 'z'; j++) {
                String nurl = url.replace("<to_replace>", ((char) i) + "" + ((char) j));
                ResponseEntity<String> entity = new RestTemplate().getForEntity(nurl, String.class);
                while (entity.getBody().contains("if you would like to have a higher API call volume")) {
                    System.out.println("waiting for 5sec due to API restriction");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException();
                    }
                    entity = new RestTemplate().getForEntity(nurl, String.class);
                }
                List<InstrId> rows = Arrays.asList(entity.getBody().split("\r\n")).stream().skip(1).map(oh -> {
                    String[] data = oh.split(",");
                    return new InstrId(data[0],
                            data[1], data[2] + "/" + data[3] + "/" + data[6] + "/" + data[7], data[0],
                            VantageDownloader.Companion.getSOURCE().name(), BigDecimal.ONE, 1, "N/A");
                }).collect(Collectors.toList());
                if(rows.size() == 0){
                    System.out.println(entity.getBody());
                }
            }
        }
    }
}
