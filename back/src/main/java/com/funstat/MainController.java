package com.funstat;

import com.funstat.domain.Annotations;
import com.funstat.domain.Ohlc;
import com.funstat.domain.TimePoint;
import com.funstat.finam.FinamDownloader;
import com.funstat.finam.Symbol;
import com.funstat.ohlc.Metadata;
import com.funstat.store.MdDao;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;


@RestController
@CrossOrigin(origins = "*")
public class MainController {

    MdDao mdDao = new MdDao();

    MdUpdater updater = new MdUpdater();

    @PostConstruct
    void onStart(){
        updater.updateSymbolsIfNeeded();
        updater.start();
    }

    ConcurrentHashMap<String,Object> cache = new ConcurrentHashMap<>();

    <T> T getThings(String key, Supplier<T> factory){
        return (T) cache.computeIfAbsent(key, k->{
            return factory.get();
        });
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
    public Collection<Symbol> instruments(){
        return getThings("instruments", ()->{
            return updater.getMeta();
        });
    }

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
            List<Ohlc> ohlcs = mdDao.queryAll(c, FinamDownloader.FINAM);
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