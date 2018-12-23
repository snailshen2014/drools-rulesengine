package com.syj.iot.rulesengine.event;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public class OrderEvent extends Event {

    private Double price;
    private String customer;

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

}
