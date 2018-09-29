package com.funstat;

import com.funstat.domain.*;
import com.funstat.ohlc.Metadata;
import com.funstat.sequenta.Sequenta;
import com.funstat.sequenta.Signal;
import com.funstat.sequenta.SignalType;
import com.funstat.store.MdDao;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@RestController
@CrossOrigin(origins = "*")
public class MainController {

    Map<Integer,String> dao = new ConcurrentHashMap<>();

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
        return Arrays.asList("TATN","ROSN","GMKN");
    }


    MdDao mdDao = new MdDao();


    @GetMapping("load_ohlc")
    public Collection<Ohlc> getChart(String code){
        return mdDao.queryAll(code);
    }

    @GetMapping("/annotations")
    public Annotations getAnnotationsForChart(String code){
        Sequenta sequenta = new Sequenta();

        List<Label> labels = new ArrayList<>();
        List<HLine> lines = new ArrayList<>();


        mdDao.queryAll(code).forEach(oh->{
            List<Signal> signals = sequenta.onOhlc(oh);
            signals.forEach(s->{
                if(s.type == SignalType.Signal){
                    labels.add(new Label("" + s.reference.countDowns.size(), oh.dateTime, s.reference.up));
                }
                if(s.type == SignalType.SetupReach){
                    lines.add(new HLine(s.reference.getStart(),s.reference.getEnd(), s.reference.getTdst()));
                }
            });
        });

        return new Annotations(labels,lines);
    }


    @GetMapping("/spread")
    public Map<String,Collection<TimePoint>> load(List<String> codes) {
        Map<String,Collection<TimePoint>> mm = new HashMap<>();
        codes.forEach(c->{
            List<Ohlc> ohlcs = mdDao.queryAll(c);
            List<TimePoint> lst = ohlcs.stream().map(oh -> new TimePoint(oh.dateTime, oh.close)).collect(Collectors.toList());
            Collections.sort(lst);
            mm.put(c, lst);
        });
        return mm;
    }

    /*
            Map<LocalDateTime,Map<String,Double>> points = new TreeMap<>();
        codes.forEach(c->{
            List<Ohlc> ohlcs = mdDao.queryAll(c);
            ohlcs.forEach(o->{
                points.computeIfAbsent(o.dateTime, a->new HashMap<>()).put(c,o.close);
            });
        });
        LocalDateTime[] index = points.keySet().toArray(new LocalDateTime[0]);

        Map<String, double[]> charts = codes.stream().collect(Collectors.toMap(c -> c, c -> {
            double[] ret = Arrays.stream(index).mapToDouble(dt -> {
                Map<String, Double> mp = points.get(dt);
                return mp.getOrDefault(c, Double.NaN);
            }).toArray();

            for(int i = 1; i < ret.length; i++){
                if(Double.isNaN(ret[i])){
                    ret[i] = ret[i - 1];
                }
                ret[i] /= ret[0];
            }
            ret[0] = 1;


            return ret;
        }));
        return new MergedSeries(index,charts);

     */


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
