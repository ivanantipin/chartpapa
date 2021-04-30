import {ContinuousMeta, Trade} from "../../api";
import {default as React, useState} from "react";
import {Slider} from "antd";
import {Filter} from "./FilterComp";

export const ContinuosSlider = (props: { meta: ContinuousMeta, onFilter: (f: Filter) => void }) => {
    const {meta, onFilter} = props

    const [range, setRange] = useState<[number, number]>([meta.min, meta.max])

    const step = (meta.max - meta.min) / 60

    return (
        <div>
            <h4>{meta.name} {range[0].toFixed(2)} - {range[1].toFixed(2)}</h4>
            <Slider range step={step} min={meta.min} max={meta.max}
                    value={[range[0], range[1]]} onChange={(val: [number, number]) => {

                const rr = val as [number, number]
                setRange(rr)

                onFilter({
                    name: meta.name,
                    filter: (t: Trade) => {
                        let val = t.continuousTags!![meta.name];
                        return rr[0] < val && rr[1] > val
                    }
                })

            }}/>
        </div>
    )
}