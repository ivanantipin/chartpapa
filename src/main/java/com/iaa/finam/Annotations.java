package com.iaa.finam;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class Annotations {
    List<Label> labels;
    List<HLine> lines;

    public Annotations(List<Label> labels, List<HLine> lines) {
        this.labels = labels;
        this.lines = lines;
    }

    @ApiModelProperty(required = true)
    public List<Label> getLabels() {
        return labels;
    }

    @ApiModelProperty(required = true)
    public List<HLine> getLines() {
        return lines;
    }
}
