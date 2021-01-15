import React from 'react';
import './App.css';
import 'antd/dist/antd.css';
import {store} from "./store";
import {Base} from "./base";
import {StoreContext} from "redux-react-hook";
import {Provider} from "react-redux";

export const App = (props: {}) => {
    return (
        <Provider store={store}>
            <StoreContext.Provider value={store}>
                <Base/>
            </StoreContext.Provider>
        </Provider>
    );
}