import {DefaultApi} from "../../api/apis";
import {Configuration} from "../../api";

export const portfoliosApi = new DefaultApi(new Configuration({basePath: process.env.REACT_APP_BACKEND_API_URL}));
export const instrumentsApi = new DefaultApi(new Configuration({basePath: process.env.REACT_APP_BACKEND_API_URL}));
export const candlesApi = new DefaultApi(new Configuration({basePath: process.env.REACT_APP_CANDLES_API_URL}))