import {DiscreteMeta, Trade} from "../../api";
import {default as React, useState} from "react";
import {sortCats} from "./FlatFactorsPage";
import {Checkbox, Slider} from "antd";
import {Filter} from "./FilterComp";

export const DiscreteSlider = (props: { meta: DiscreteMeta, onFilter: (f: Filter) => void }) => {
    const {meta, onFilter} = props

    const [range, setRange] = useState([0, meta.values.length - 1])

    const [inversed, setInversed] = useState(false)

    let mapped = sortCats(meta.values);

    const mappedIndex: { [key: string]: number } = {}

    const marks: { [key: number]: string } = {}

    for (let idx = 0; idx < mapped.length; idx++) {
        let str = mapped[idx];
        marks[idx] = str
        mappedIndex[str] = idx
    }

    return (
        <div>
            <h4>{meta.name} {marks[range[0]]} - {marks[range[1]]}</h4>
            <Checkbox checked={inversed} onChange={e => setInversed(e.target.checked)}>inversed</Checkbox>
            <Slider marks={marks} step={null} range min={0} max={meta.values.length - 1}
                    value={[range[0], range[1]]} onChange={(val: [number, number]) => {
                const range = val as [number, number]
                setRange(range)
                onFilter({
                    name: meta.name,
                    filter: (t: Trade) => {
                        const idx = mappedIndex[t.discreteTags!![meta.name]]
                        return inversed !== (range[0] <= idx && range[1] >= idx);
                    }
                })

            }
            }/>

        </div>
    )
}