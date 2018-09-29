import {timeFormat, timeParse} from "d3-time-format";
import {HLine, Label} from "../../api";
import * as React from "react";
import {format} from "d3-format";
import {Map} from "immutable";

let {last} = require("react-stockcharts/lib/utils");
let {OHLCTooltip} = require("react-stockcharts/lib/tooltip");
let {Annotate, Label, LabelAnnotation} = require("react-stockcharts/lib/annotation");
let {Chart, ChartCanvas} = require("react-stockcharts");
let {discontinuousTimeScaleProvider} = require("react-stockcharts/lib/scale");
let fitWidth = require("react-stockcharts/lib/helper");
let {XAxis, YAxis} = require("react-stockcharts/lib/axes");
let {CrossHairCursor, EdgeIndicator, MouseCoordinateX, MouseCoordinateY} = require("react-stockcharts/lib/coordinates");
let TrendLine = require("react-stockcharts/es/lib/interactive/TrendLine")
let CandlestickSeries = require("react-stockcharts/lib/series");


interface Ohlc{
    date : Date,
    open : number,
    high : number,
    low : number,
    close : number,
    volume : number
}

interface CandleStickChartProps{
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
            y: (obj : any) => {
                const high = this.props.tsToLabel.get(obj.datum.date.getTime()).high;
                if(high){
                    return obj.yScale(obj.datum.high)
                }else{
                    return obj.yScale(obj.datum.low) + 15
                }
            },
            onClick: console.log.bind(console),
            tooltip: (d : any) => timeFormat("%B")(d.date)
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
            (d : any) => d.date
        );
        const { data, xScale, xAccessor, displayXAccessor } = xScaleProvider(
            initialData
        );

        console.log('data',data);

        const start = xAccessor(last(data));
        const end = xAccessor(data[Math.max(0, data.length - 150)]);
        const xExtents = [start, end];


        const dtTo : Map<number,any> = Map();

        data.forEach((d : any)=>{
            dtTo.set(d.date.getTime(), d)
        });

        console.log('hliens',this.props.hlines);

        let parser = timeParse("%Y-%m-%dT%H:%M:%S");

        const parseDate=(str : string)=>{
            return parser(str) || new Date()
        }

        const trends_1 = this.props.hlines.map(line=>{
            const x0 = xAccessor(dtTo.get(parseDate(line.start).getTime()));
            const x1 = xAccessor(dtTo.get(parseDate(line.end).getTime()));
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
                    yExtents={[(d : any) => [d.high, d.low]]}
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
                        yAccessor={(d : any) => d.close}
                        fill={(d : any) => (d.close > d.open ? "#6BA583" : "#FF0000")}
                    />

                    <TrendLine
                        //ref={this.saveInteractiveNodes("Trendline", 1)}
                        enabled={false}
                         type="LINE"
                        snap={false}
                        snapTo={(d : Ohlc) => [d.high, d.low]}
                        onStart={() => console.log("START")}
                        //onComplete={this.onDrawCompleteChart1}
                        trends={trends_1}
                    />

                    <OHLCTooltip origin={[-40, 0]} />

                    <Annotate
                        with={LabelAnnotation}
                        when={(d : {date : Date}) => {
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
