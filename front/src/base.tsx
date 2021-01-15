import React, {useEffect} from 'react';
import './App.css';
import {Layout} from 'antd';
import 'antd/dist/antd.css';
import {SiderMenu} from "./components/sider/SiderMenu";
import {Header} from "./components/header/Header";
import {BrowserRouter as Router, Route, Switch} from "react-router-dom";
import {TotalPage} from "./pages/total/TotalPage";
import {useDispatch} from "react-redux";
import {setPortfolios} from "./actions/actions";
import {portfoliosApi} from "./services/api/services";
import {BySymbolPage} from "./pages/by-symbol/BySymbolPage";
import {AggregationPage} from "./pages/aggregation/AggregationPage";

const {Content} = Layout;

export const Base = (props: any) => {

    let dispatch = useDispatch();

    useEffect(() => {
        portfoliosApi.portfoliosList().then(lst => {
            dispatch(setPortfolios(lst))
        })
    }, [])


    return (
        <Router>
            <Switch>
                <Layout style={{height: '2000px'}}>
                    <SiderMenu/>
                    <Layout className="site-layout">
                        <Header/>
                        <Content style={{padding: '0 10px', height: '100%'}}>
                            <div className="site-layout-content">
                                <Route path='/total'>
                                    <TotalPage/>
                                </Route>
                                <Route path='/by-symbol'>
                                    <BySymbolPage/>
                                </Route>
                                <Route path='/aggregation'>
                                    <AggregationPage/>
                                </Route>
                            </div>
                        </Content>
                    </Layout>
                </Layout>
            </Switch>
        </Router>
    )
}