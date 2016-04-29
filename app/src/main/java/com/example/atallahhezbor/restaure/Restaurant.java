package com.example.atallahhezbor.restaure;

import java.util.ArrayList;

/**
 * Created by atallahhezbor on 4/27/16.
 */
public class Restaurant {

    private String name;
    private String filepath;
    private String desc;


    public Restaurant() {
        this.name = "";
        this.filepath = "";
        this.desc = "";
    }
    public Restaurant(String name, String filepath, String desc) {
        this.name = name;
        this.filepath = filepath;
        this.desc = desc;
    }
    public Restaurant(ArrayList<String> fields) {
        this.name = fields.get(0);
        this.filepath = fields.get(1);
        this.desc = fields.get(2);
    }


    public String getName() {
        return name;
    }
    public String getDesc() {
        return desc;
    }
    public String getFilepath() {
        return filepath;
    }



}
