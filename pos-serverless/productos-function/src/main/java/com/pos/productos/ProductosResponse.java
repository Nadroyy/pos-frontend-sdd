package com.pos.productos;

import java.util.List;

public class ProductosResponse {

    private List<Product> products;
    private Integer count;

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
