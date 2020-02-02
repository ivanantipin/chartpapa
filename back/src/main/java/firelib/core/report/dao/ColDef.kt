package firelib.core.report.dao

import kotlin.reflect.KType
import kotlin.reflect.jvm.reflect

class ColDef<I,T>(val name : String, val extract : (I)->T, val typeOverride : KType? = null){
    fun getSqlType(): String{
        return SqlTypeMapper.mapType(
            typeOverride ?: extract.reflect()!!.returnType
        )
    }

}