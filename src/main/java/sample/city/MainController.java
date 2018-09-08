package sample.city;

import com.iaa.finam.MdDao;
import com.iaa.finam.MergedSeries;
import com.iaa.finam.Ohlc;
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


/*
        response.headers["Access-Control-Allow-Origin"] = "*"
        response.headers["Access-Control-Allow-Headers"] = "Origin, X-Requested-With, Content-Type, Accept"

 */

@RestController
@CrossOrigin(origins = "*")
public class MainController {

    static {
        System.out.println("aoeuaoeuaoeu");
    }


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


    @GetMapping("/spread")
    public MergedSeries load(List<String> codes) {
        Map<LocalDateTime,Map<String,Double>> points = new TreeMap<>();
        codes.forEach(c->{
            List<Ohlc> ohlcs = mdDao.queryAll(c);
            ohlcs.forEach(o->{
                points.computeIfAbsent(o.time, a->new HashMap<>()).put(c,o.close);
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
    }


    @Bean
    public Docket customImplementation() {
     return new Docket(DocumentationType.SWAGGER_2)
                 //here is the line that you need
                .directModelSubstitute(LocalDateTime.class, Date.class)
                .directModelSubstitute(LocalDate.class, Date.class)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.regex("/.*"))
                .build()
                .pathMapping("/");    }



}
