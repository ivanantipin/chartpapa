import {DefaultApi} from "../../api";
import {Configuration} from "../../api";

const url = "http://localhost:8080"

export const portfoliosApi = new DefaultApi(new Configuration({basePath: url}));
export const instrumentsApi = new DefaultApi(new Configuration({basePath: url}));
export const candlesApi = new DefaultApi(new Configuration({basePath: url}))