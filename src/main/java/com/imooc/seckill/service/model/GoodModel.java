package com.imooc.seckill.service.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class GoodModel {
    private Integer id;

    @NotBlank(message = "title cannot empty")
    private String title;

    @NotNull(message = "price cannot be null")
    @Min(value = 0, message = "price cannot be negative")
    private BigDecimal price;

    @NotNull(message = "stock cannot be null")
    private Integer stock;

    @NotNull(message = "description cannot be null")
    private String description;

    @NotNull(message = "image url cannot be null")
    private String imageUrl;

    @NotNull(message = "sales cannot be null")
    @Min(value = 0, message = "sales cannot be negatvie")
    private Integer sales;

    // if eventModel is not null, it has coming or in-progress event
    private EventModel eventModel;

    public Integer getId() {
        return id;
    }

    public EventModel getEventModel() {
        return eventModel;
    }

    public void setEventModel(EventModel eventModel) {
        this.eventModel = eventModel;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }
}
