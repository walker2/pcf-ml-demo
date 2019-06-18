package org.ashipilo.nndashboard.job;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
public class Task implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String name;

    private String type;

    private String status;

    private String desc;

    private String cronExp;

    private List<String> files;

    private String train_buck;

    private String valid_buck;

    private String interfaceName;

    private String execution;

    private String result = "";

}
