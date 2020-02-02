package firelib.core

import firelib.model.Model
import firelib.model.ModelContext

typealias ModelFactory = (context : ModelContext, props : Map<String,String>) -> Model