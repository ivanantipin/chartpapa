package com.iaa.finam;

import com.google.common.util.concurrent.SettableFuture;
import org.apache.commons.io.IOUtils;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.io.CharStreams.readLines;


public class FinamDownloader {
    private static final Logger log = LoggerFactory.getLogger(FinamDownloader.class);

    private AsyncHttpClient client;


    public FinamDownloader() {
        this.client = new DefaultAsyncHttpClient(
                new DefaultAsyncHttpClientConfig.Builder()
                        .setFollowRedirect(true)
                        .setKeepAlive(true)
                        .setConnectionTtl(5000)
                        .setRequestTimeout(180000)
                        .setMaxRequestRetry(3)
                        .build()
        );
    }

    public List<Ohlc> load(Symbol symbolSpec, LocalDate start) {
        LocalDate finish = LocalDate.now();
        Map<String, String> params = new HashMap<String, String>() {
            {
                put("d", "d");
                //put("f", "table");
                put("e", ".csv");
                put("dtf", "1");
                put("tmf", "3");
                put("MSOR", "0");
                put("mstime", "on");
                put("mstimever", "1");
                put("sep", "3");
                put("sep2", "1");
                put("at", "1");
            }
        };
        StringBuilder url = new StringBuilder("http://export.finam.ru/table.csv?f=table");
        params.put("p", "" + Period.DAILY.getId());
        params.put("em", "" + symbolSpec.id);
        params.put("market", symbolSpec.market);

        params.put("df", "" + start.getDayOfMonth());
        params.put("mf", "" + (start.getMonthValue() - 1));
        params.put("yf", "" + start.getYear());

        params.put("dt", "" + finish.getDayOfMonth());
        params.put("mt", "" + (finish.getMonthValue() - 1));
        params.put("yt", "" + finish.getYear());

        params.put("code", "" + symbolSpec.code);
        params.put("cn", "" + symbolSpec.code);

        params.put("from", start.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        params.put("to", finish.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        params.forEach((k, v) -> {
            url.append("&" + k + "=" + v);
        });
        SettableFuture<List<String>> ret = SettableFuture.create();
        client.prepareGet(url.toString()).execute()
                .toCompletableFuture()
                .thenAccept(response -> {
                    log.info("Status", response.getStatusCode());
                    try {
                        List<String> lines = readLines(new InputStreamReader(response.getResponseBodyAsStream(), "cp1251"));
                        ret.set(lines);
                  } catch (IOException e) {
                        log.error("Can't read data", e);
                    }
                });

        try {
            return ret.get().stream().map(Ohlc::parse).flatMap(opt -> {
                if (opt.isPresent()) {
                    return Stream.of(opt.get());
                } else {
                    return Stream.empty();
                }
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        try {
            client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    static void populate(String inl, Map<String, String[]> map) {
        if (inl.indexOf('[') < 0) return;

        String[] data = inl.substring(inl.indexOf('[') + 1, inl.indexOf(']')).split(",");
        String key = inl.substring(0, inl.indexOf('['))
                .replace(" ", "")
                .replace("=", "");
        map.put(key, data);
    }


    /*
    0 = "varaEmitentNames"
1 = "varaEmitentMarkets"
2 = "varaEmitentIds"
3 = "varaDataFormatStrs"
4 = "varaEmitentChild"
5 = "varaEmitentCodes"
     */

    public List<Symbol> readMeta() {
        try {
            InputStream in = new URL("https://www.finam.ru/cache/icharts/icharts.js").openStream();
            List<String> lines = IOUtils.readLines(in);

            HashMap<String, String[]> map = new HashMap<>();
            lines.forEach(l -> {
                populate(l, map);
            });

            String[] names = map.get("varaEmitentNames");
            String[] ids = map.get("varaEmitentIds");
            String[] codes = map.get("varaEmitentCodes");
            String[] markets = map.get("varaEmitentMarkets");

            List<Symbol> ret = new ArrayList<>();

            for (int i = 0; i < codes.length; i++) {
                ret.add(new Symbol(ids[i], "na", markets[i], codes[i].replace("'","")));
            }

            System.out.println(map);
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

