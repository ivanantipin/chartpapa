package firelib.stockviz.api

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/api/v1")
class InstrumentsApiController {

    @Get(value = "/instruments/",produces = ["application/json"])
    fun instrumentsList(): List<Instrument> {
        return emptyList()
    }
}
