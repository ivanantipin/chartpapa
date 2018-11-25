package com.funstat;

import com.funstat.domain.Annotations;
import com.funstat.domain.Ohlc;
import com.funstat.domain.TimePoint;
import com.funstat.finam.InstrId;
import com.funstat.ohlc.Metadata;
import com.funstat.store.MdStorage;
import com.funstat.store.MdStorageImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@CrossOrigin(origins = "*")
public class MainController {

    MdStorage storage = new MdStorageImpl("/ddisk/globaldatabase/md");

    @PostConstruct
    void onStart(){
        storage.updateSymbolsMeta();
    }

    List<Metadata> allMetas = new ArrayList<>();

    @GetMapping(value = "/metas")
    public List<Metadata> loadAllMetas(){
        return allMetas;
    }

    @PutMapping("/put_meta")
    public void addMetadata(@Valid @RequestParam Metadata metadata){
        allMetas.add(metadata);
    }

    @RequestMapping(value="/instruments", method=RequestMethod.GET)
    public Collection<InstrId> instruments(){
        return storage.getMeta();
    }

    @PostMapping("/get_ohlcs")
    public Collection<Ohlc> getOhlcs(@RequestBody @Valid InstrId instrId, String interval){
        return storage.read(instrId, interval);
    }

    @PostMapping("/get_annotations")
    public Annotations getAnnotations(@RequestBody @Valid InstrId instrId, String interval){
        List<Ohlc> ohlcs = storage.read(instrId, interval);
        return AnnotationCreator.createAnnotations(ohlcs);
    }

    @PostMapping("/get_series")
    public Map<String,Collection<TimePoint>> getSeries(@RequestBody @Valid InstrId[] codes, String interval) {
        Map<String,Collection<TimePoint>> mm = new HashMap<>();
        Arrays.stream(codes).forEach(c->{
            List<TimePoint> lst = storage.read(c,interval).stream().map(oh -> new TimePoint(oh.dateTime, oh.close)).collect(Collectors.toList());
            Collections.sort(lst);
            mm.put(c.code, lst);
        });
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