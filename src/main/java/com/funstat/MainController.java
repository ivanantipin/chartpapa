package com.funstat;

import com.funstat.domain.*;
import com.funstat.finam.Symbol;
import com.funstat.ohlc.Metadata;
import com.funstat.sequenta.Sequenta;
import com.funstat.sequenta.Signal;
import com.funstat.sequenta.SignalType;
import com.funstat.store.MdDao;
import com.funstat.vantage.VantageDownloader;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;


@RestController
@CrossOrigin(origins = "*")
public class MainController {

    Map<Integer,String> dao = new ConcurrentHashMap<>();

    MdUpdater updater = new MdUpdater();

    @PostConstruct
    void onStart(){
        System.out.println("starting updater");
        updater.start();
    }

    ConcurrentHashMap<String,Object> cache = new ConcurrentHashMap<>();

    <T> T getThings(String key, Supplier<T> factory){
        return (T) cache.computeIfAbsent(key, k->{
            return factory.get();
        });
    }

    @GetMapping("/")
    public Collection<String> selectAll() {
        return dao.values();
    }

    @PutMapping("/update")
    public Collection<String> updateAndSelectAll(
            @RequestParam(value = "id", defaultValue = "1") int id,
            @RequestParam("name") String name) {
        dao.put(id,name);
        return dao.values();
    }

    List<Metadata> allMetas = new ArrayList<>();

    @GetMapping(value = "/metas")
    public List<Metadata> loadAllMetas(){
        return allMetas;
    }

    @PutMapping("/put_meta")
    public void addMetadata(@RequestParam("metadata") Metadata metadata){
        allMetas.add(metadata);
    }

    @RequestMapping(value="/instruments", method=RequestMethod.GET)
    public Collection<String> instruments(){
        return getThings("instruments", ()->{
            return updater.getMeta().stream().filter(s->{
                return s.market.equals("1") || s.market.equals(VantageDownloader.MICEX);
            })                    .map(s->s.code).collect(Collectors.toList());
        });
    }

    MdDao mdDao = new MdDao();


    @GetMapping("/get_ohlcs")
    public Collection<Ohlc> getOhlcs(String code){
        return updater.get(code);
    }

    @GetMapping("/get_annotations")
    public Annotations getAnnotations(String code){
        return AnnotationCreator.createAnnotations(code,updater);
    }

    @GetMapping("/get_series")
    public Map<String,Collection<TimePoint>> getSeries(@RequestParam(name = "codes")  ArrayList<String> codes) {
        Map<String,Collection<TimePoint>> mm = new HashMap<>();
        codes.forEach(c->{
            List<Ohlc> ohlcs = mdDao.queryAll(c);
            List<TimePoint> lst = ohlcs.stream().map(oh -> new TimePoint(oh.dateTime, oh.close)).collect(Collectors.toList());
            Collections.sort(lst);
            mm.put(c, lst);
        });
        System.out.println("return" + mm);
        return mm;
    }

    @Bean
    public Docket customImplementation() {
     return new Docket(DocumentationType.SWAGGER_2)
                 //here is the line that you need
                .directModelSubstitute(LocalDateTime.class, String.class)
                .directModelSubstitute(LocalDate.class, String.class)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.regex("/.*"))
                .build()
                .pathMapping("/");    }
}