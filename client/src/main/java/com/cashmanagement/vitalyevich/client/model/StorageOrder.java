package com.cashmanagement.vitalyevich.client.model;

import java.time.LocalDate;

public class StorageOrder {

    private Integer id;

    private Order order;

    private LocalDate orderDate;

    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public StorageOrder() {
    }

    public StorageOrder(Integer id, Order order, LocalDate orderDate, User user) {
        this.id = id;
        this.order = order;
        this.orderDate = orderDate;
        this.user = user;
    }

    public StorageOrder(Order order, LocalDate orderDate, User user) {
        this.order = order;
        this.orderDate = orderDate;
        this.user = user;
    }
}