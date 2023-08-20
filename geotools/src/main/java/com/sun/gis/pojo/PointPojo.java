package com.sun.gis.pojo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author sunbt
 * @date 2023/8/20 0:56
 */
@Data
public class PointPojo {

    @ApiModelProperty(value = "x坐标")
    private String x;

    @ApiModelProperty(value = "y坐标")
    private String y;

}
