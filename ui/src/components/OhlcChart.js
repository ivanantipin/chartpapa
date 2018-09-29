// @flow
import React from "react";
import PropTypes from "prop-types";

import { format } from "d3-format";
import {timeFormat, timeParse} from "d3-time-format";

import {ChartCanvas, Chart} from "react-stockcharts";
import { CandlestickSeries, LineSeries } from "react-stockcharts/lib/series";
import { XAxis, YAxis } from "react-stockcharts/lib/axes";
import {
    CrossHairCursor,
    EdgeIndicator,
    CurrentCoordinate,
    MouseCoordinateX,
    MouseCoordinateY
} from "react-stockcharts/lib/coordinates";

import {
    LabelAnnotation,
    Label,
    Annotate
} from "react-stockcharts/lib/annotation";
import { discontinuousTimeScaleProvider } from "react-stockcharts/lib/scale";
import {
    OHLCTooltip,
    MovingAverageTooltip
} from "react-stockcharts/lib/tooltip";
import { ema } from "react-stockcharts/lib/indicator";
import { fitWidth } from "react-stockcharts/lib/helper";
import { last } from "react-stockcharts/lib/utils";
import {PriceCoordinate} from "react-stockcharts/es/lib/coordinates/index";
import StraightLine from "react-stockcharts/es/lib/series/StraightLine";
import TrendLine from "react-stockcharts/es/lib/interactive/TrendLine"
import type {HLine} from "../api";


export type Ohlc={
    date : Date,
    open : number,
    high : number,
    low : number,
    close : number,
    volume : number
}

export type CandleStickChartProps={
    data: Array<Ohlc>,
    width: number,
    ratio: number,
    type: "svg" | "hybrid",
    tsToLabel: Map<number,Label>,
    hlines : Array<HLine>
}



class CandleStickChart extends React.Component<CandleStickChartProps> {
    render() {
        const annotationProps = {
            fontFamily: "Glyphicons Halflings",
            fontSize: 15,
            fill: "#060F8F",
            opacity: 0.8,
            text: (some : {date : Date})=>{
                return this.props.tsToLabel.get(some.date.getTime()).text
            },
            y: (obj) => {
                const high = this.props.tsToLabel.get(obj.datum.date.getTime()).high;
                if(high){
                    return obj.yScale(obj.datum.high)
                }else{
                    return obj.yScale(obj.datum.low) + 15
                }
            },
            onClick: console.log.bind(console),
            tooltip: d => timeFormat("%B")(d.date)
            // onMouseOver: console.log.bind(console),
        };

        const margin = { left: 80, right: 80, top: 30, bottom: 50 };
        const height = 400;
        const { type, data: initialData, width, ratio } = this.props;

        const [yAxisLabelX, yAxisLabelY] = [
            width - margin.left - 40,
            (height - margin.top - margin.bottom) / 2
        ];

        const xScaleProvider = discontinuousTimeScaleProvider.inputDateAccessor(
            d => d.date
        );
        const { data, xScale, xAccessor, displayXAccessor } = xScaleProvider(
            initialData
        );

        console.log('data',data);

        const start = xAccessor(last(data));
        const end = xAccessor(data[Math.max(0, data.length - 150)]);
        const xExtents = [start, end];


        const dtTo : Map<number,any> = new Map();

        data.forEach(d=>{
            dtTo.set(d.date.getTime(), d)
        });

        console.log('hliens',this.props.hlines);

        let parser = timeParse("%Y-%m-%dT%H:%M:%S");

        const trends_1 = this.props.hlines.map(line=>{
            const x0 = xAccessor(dtTo.get(parser(line.start).getTime()));
            const x1 = xAccessor(dtTo.get(parser(line.end).getTime()));
            return {
                start : [x0, line.level],
                end : [x1, line.level],
                appearance: { stroke: "green" }, type: "LINE"
            }
        });

        return (
            <ChartCanvas
                height={height}
                ratio={ratio}
                width={width}
                margin={margin}
                type={type}
                seriesName="MSFT"
                data={data}
                xScale={xScale}
                xAccessor={xAccessor}
                displayXAccessor={displayXAccessor}
                xExtents={xExtents}
            >
                <Label
                    x={(width - margin.left - margin.right) / 2}
                    y={30}
                    fontSize="30"
                    text="Chart title here"
                />

                <Chart
                    id={1}
                    yExtents={[d => [d.high, d.low]]}
                    padding={{ top: 10, bottom: 20 }}
                >
                    <XAxis axisAt="bottom" orient="bottom" />
                    <MouseCoordinateX
                        at="bottom"
                        orient="bottom"
                        displayFormat={timeFormat("%Y-%m-%d")}
                    />
                    <MouseCoordinateY
                        at="right"
                        orient="right"
                        displayFormat={format(".2f")}
                    />

                    <Label
                        x={(width - margin.left - margin.right) / 2}
                        y={height - 45}
                        fontSize="12"
                        text="XAxis Label here"
                    />

                    <YAxis axisAt="right" orient="right" ticks={5} />

                    <Label
                        x={yAxisLabelX}
                        y={yAxisLabelY}
                        rotate={-90}
                        fontSize="12"
                        text="YAxis Label here"
                    />

                    <CandlestickSeries />
                    <EdgeIndicator
                        itemType="last"
                        orient="right"
                        edgeAt="right"
                        yAccessor={d => d.close}
                        fill={d => (d.close > d.open ? "#6BA583" : "#FF0000")}
                    />

                    {/*{
                        this.props.hlines.map(line=>{
                            console.log('hline',line)
                            return (
                                <PriceCoordinate
                                    at="right"
                                    orient="right"
                                    price={line.level}
                                    stroke="#3490DC"
                                    strokeWidth={1}
                                    fill="#FFFFFF"
                                    textFill="#22292F"
                                    arrowWidth={7}
                                    strokeDasharray="ShortDash"
                                    displayFormat={format(".2f")}
                                />
                            )
                        })
                    }*/}


                    <TrendLine
                        //ref={this.saveInteractiveNodes("Trendline", 1)}
                        enabled={false}
                         type="LINE"
                        snap={false}
                        snapTo={d => [d.high, d.low]}
                        onStart={() => console.log("START")}
                        //onComplete={this.onDrawCompleteChart1}
                        trends={trends_1}
                    />



                    <OHLCTooltip origin={[-40, 0]} />

                    <Annotate
                        with={LabelAnnotation}
                        when={d => {
                            return this.props.tsToLabel.has(d.date.getTime())
                        }}
                        usingProps={annotationProps}
                    />
                </Chart>
                <CrossHairCursor strokeDasharray="LongDashDot" />
            </ChartCanvas>
        );
    }
}

export default fitWidth(CandleStickChart);
