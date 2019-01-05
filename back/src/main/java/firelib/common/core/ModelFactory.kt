package firelib.common.core

import firelib.common.model.Model
import firelib.common.model.ModelContext

typealias ModelFactory = (context : ModelContext, props : Map<String,String>) -> Model