export enum QuantinizeAggType {
    Quantile,
    Range
}

export type QuantinizeFunction = (val: number) => string

function sortNumber(a: number, b: number) {
    return a - b
}

export function GetContQuantizationFunc(valuesArr: Array<number>, numGroups: number, qType: QuantinizeAggType): QuantinizeFunction {
    let splitPointsArray: Array<number> = []
    if (qType === QuantinizeAggType.Quantile) {
        splitPointsArray = getQuantiles(valuesArr, numGroups)
    } else {
        splitPointsArray = getRanges(valuesArr, numGroups)
    }
    splitPointsArray.sort(sortNumber)

    return (val: number): string => {
        let lowerBand = splitPointsArray[0]
        for (const p of splitPointsArray) {
            if (val <= p) {
                return getStringValueFromRange(lowerBand, p)
            }
            lowerBand = p
        }

        return `${getStringByValue(lowerBand)}-inf`
    }
}

function quantile(array: Array<number>, percentile: number): number {
    let index = percentile / 100.0 * (array.length - 1)
    if (Math.floor(index) == index) {
        return array[index]
    } else {
        const i = Math.floor(index)
        const fraction = index - i
        return array[i] + (array[i + 1] - array[i]) * fraction
    }
}

function getQArrayFromQNumber(numQuantiles: number): Array<number> {
    const qStep = 100.0 / numQuantiles
    console.log(qStep)
    const qBlocks: Array<number> = []
    let lastQ = 0
    while (true) {
        if (lastQ >= 100) {
            break
        }
        lastQ += qStep
        lastQ = Math.min(lastQ, 100)
        if (100 - lastQ < 0.5 * qStep) {
            qBlocks.push(100)
            break
        }
        qBlocks.push(lastQ)
    }
    return qBlocks
}

function getQuantiles(array: Array<number>, numQuantiles: number): Array<number> {
    array.sort(sortNumber)
    const qArray = getQArrayFromQNumber(numQuantiles)
    const quantilesValues: Array<number> = []
    for (const q of qArray) {
        quantilesValues.push(quantile(array, q))
    }
    return quantilesValues
}

function getRanges(valuesArr: Array<number>, groups: number): Array<number> {
    valuesArr.sort(sortNumber)
    const min = valuesArr[0]
    const max = valuesArr[valuesArr.length - 1]
    const rStep = (max - min) / groups
    const rBlocks: Array<number> = []
    let lastQ = min
    while (true) {
        if (lastQ >= max) {
            break
        }
        lastQ += rStep
        lastQ = Math.min(lastQ, max)
        if (max - lastQ < 0.5 * rStep) {
            rBlocks.push(max)
            break
        }
        rBlocks.push(lastQ)
    }
    return rBlocks
}


function getStringValueFromRange(p1: number, p2: number): string {
    return `${getStringByValue(p1)}-${getStringByValue(p2)}`
}

function getStringByValue(val: number): string {
    const absVal = Math.abs(val)
    if (absVal < 0.2) {
        return `${val.toFixed(3)}`
    }
    if (absVal < 1) {
        return `${val.toFixed(2)}`
    }

    if (absVal < 100) {
        return `${Math.round(val)}`
    }

    if (absVal < 1000) {
        return `${Math.round(val / 100) * 100}`
    }
    if (absVal < 10000) {
        return `${Math.round(val / 500) * 500}`
    }
    return `${Math.round(val / 1000)}K`
}





