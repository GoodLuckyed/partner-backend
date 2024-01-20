package com.yuyan.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * @author lucky
 * @date 2024/1/21
 */
@Data
public class UpdateTagRequest implements Serializable {
    private static final long serialVersionUID = 8874832449800105639L;

    private long id;
    private Set<String> tagList;
}
