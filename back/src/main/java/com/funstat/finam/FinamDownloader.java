package com.funstat.finam;

import com.funstat.domain.Ohlc;
import com.funstat.vantage.Source;
import com.google.common.util.concurrent.SettableFuture;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
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
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.io.CharStreams.readLines;


public class FinamDownloader implements AutoCloseable, Source {
    private static final Logger log = LoggerFactory.getLogger(FinamDownloader.class);
    public static final String FINAM = "FINAM";

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



    static void populate(String inl, Map<String, String[]> map) {
        if (inl.indexOf('[') < 0) return;
        String origStr = inl.substring(inl.indexOf('[') + 1, inl.indexOf(']'));
        String[] data = origStr.split(",");
        String key = inl.substring(0, inl.indexOf('['))
                .replace(" ", "")
                .replace("=", "");

        if (key.equals("varaEmitentNames")) {
            CSVParser parser = new CSVParserBuilder().withQuoteChar('\'').build();
            try {
                data = parser.parseLine(origStr);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        map.put(key, data);
    }


    @Override
    public void close() throws Exception {
        try {
            client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Symbol> symbols() {
        try {
            InputStream in = new URL("https://www.finam.ru/cache/icharts/icharts.js").openStream();
            List<String> lines = IOUtils.readLines(in, Charset.forName("cp1251"));

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
                ret.add(new Symbol(ids[i], names[i], markets[i], codes[i].replace("'", ""), getName()));
            }

            System.out.println(map);
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Ohlc> load(Symbol symbolSpec) {
        return load(symbolSpec, LocalDateTime.now().minusDays(600));
    }

    volatile long lastFinamCall  = 0;

    @Override
    public synchronized List<Ohlc> load(Symbol symbolSpec, LocalDateTime start) {

        while ((System.currentTimeMillis() - lastFinamCall) < 1100){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        lastFinamCall = System.currentTimeMillis();


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
            Stream<String> str = ret.get().stream();
            return str.map(Ohlc::parse).flatMap(opt -> {
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

    @Override
    public String getName() {
        return FINAM;
    }
}

