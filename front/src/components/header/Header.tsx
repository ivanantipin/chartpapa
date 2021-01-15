import React, {useEffect} from "react";
import {Button, PageHeader, Select, Spin} from "antd";
import {useDispatch} from "react-redux"
import {
    setAvailableInstrumentsMeta,
    setAvailableTags,
    setInstruments,
    setLoadStatus,
    setOrders,
    setPortfolioID,
    setTrades
} from "../../actions/actions"
import {useMappedState} from "redux-react-hook";
import {IMainState, loadingLoadingState, LoadStatus, LoadStatusEnum} from "../../reducers/reducers";
import {instrumentsApi, portfoliosApi} from "../../services/api/services";
import {
    getLocalStoredInstruments,
    setLocalStoredInstruments,
    setStoredPortfolioAvailableTags,
    setStoredPortfolioInstrumentsMeta
} from "../../services/localStorage";

const {Option} = Select;

export const portfolioStorage = window.localStorage;


const StatusComp = (props: { status: LoadStatus }) => {
    let values = Object.values(props.status);
    if (values.some(it => it === LoadStatusEnum.Error)) {
        return <div>Error load</div>
    }
    if (values.some(it => it === LoadStatusEnum.Loading)) {
        return <Spin/>
    }
    return <div>Loaded</div>
};


export const Header = (props: any) => {

    const {availablePortfolios, portfolioID, loadStatus} = useMappedState((state: IMainState) => state)


    let dispatch = useDispatch();

    const onPortfolioSelected = (value: string) => {
        if (value) {
            portfolioStorage.setItem("portfolio", value);
            dispatch(setPortfolioID(value));
        }
    };

    useEffect(() => {
        let storedPortfolio = portfolioStorage.getItem("portfolio");
        if (!portfolioID && storedPortfolio) {
            onPortfolioSelected(storedPortfolio)
        }

        if (portfolioID) {
            dispatch(setLoadStatus(loadingLoadingState))

            portfoliosApi.portfoliosTradesList({portfolio: portfolioID}).then(trd => {
                dispatch(setTrades(trd))
                dispatch(setLoadStatus({trades: LoadStatusEnum.Loaded}))
            });


            portfoliosApi.portfoliosOrdersList({portfolio: portfolioID}).then(orders => {
                dispatch(setOrders(orders))
                dispatch(setLoadStatus({orders: LoadStatusEnum.Loaded}))
            });

            console.log('Loading meta and tags from API')
            portfoliosApi.portfoliosAvailableInstrumentsMetaList(
                {portfolio: portfolioID}).then(meta => {
                setStoredPortfolioInstrumentsMeta(portfolioID, meta)
                dispatch(setAvailableInstrumentsMeta(meta))
                dispatch(setLoadStatus({instruments: LoadStatusEnum.Loaded}))
            });
            portfoliosApi.portfoliosAvailableTagsList({portfolio: portfolioID}).then(tags => {
                setStoredPortfolioAvailableTags(portfolioID, tags)
                dispatch(setAvailableTags(tags))
                dispatch(setLoadStatus({tags: LoadStatusEnum.Loaded}))
            });


        }
    }, [portfolioID])

    useEffect(() => {
        dispatch(setInstruments(getLocalStoredInstruments()))
        dispatch(setLoadStatus({instruments: LoadStatusEnum.Loaded}))
        instrumentsApi.instrumentsList().then(instruments => {
            setLocalStoredInstruments(instruments)
            dispatch(setInstruments(instruments))
            dispatch(setLoadStatus({instruments: LoadStatusEnum.Loaded}))
        })
    }, [])


    let options = null;

    if (availablePortfolios !== undefined) {
        options = [];
        availablePortfolios.map(p => {
            options.push(<Option key={p.name} value={p.name}>{p.name}</Option>)
        })
    }

    const title = portfolioID === undefined ? "Please choose portfolio" : `Results for portfolio ${portfolioID}`;

    return (
        <PageHeader
            ghost={false}
            onBack={() => window.history.back()}
            title={title}
            extra={[
                <StatusComp status={loadStatus}/>,
                <Button key="1">Manage Portfolios</Button>,
                <Select key="2" style={{width: 200}} defaultValue="" value={portfolioID} onSelect={onPortfolioSelected}>
                    <Option value="">Please select portfolio...</Option>
                    {options}
                </Select>
            ]}
        />
    )
};

