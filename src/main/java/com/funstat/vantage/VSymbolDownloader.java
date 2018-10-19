package com.funstat.vantage;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.stream.Collectors;

public class VSymbolDownloader {

    public static void main(String[] args) {
        String url = "https://www.alphavantage.co/query?function=SYMBOL_SEARCH&keywords=<to_replace>&apikey=P28H4WI1MIPJPGBP&datatype=csv";
        String kurl = "https://www.alphavantage.co/query?function=SYMBOL_SEARCH&keywords=ra&apikey=P28H4WI1MIPJPGBP&datatype=csv";




        int aa = 'a';



        for(int i = 97; i <= 'z'; i++){
            for(int j = 97; j <= 'z'; j++){
                String nurl = url.replace("<to_replace>", ((char) i) + "" + ((char) j));
                ResponseEntity<String> entity = new RestTemplate().getForEntity(nurl, String.class);
                Arrays.asList(entity.getBody().split("\r\n")).stream().forEach(oh -> {
                    System.out.println(oh);
                });
            }
        }




    }
}
