package com.imooc.seckill.entity;

import java.math.BigDecimal;
import java.util.Date;

public class Event {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column event_tbl.id
     *
     * @mbg.generated Tue Dec 01 10:47:07 PST 2020
     */
    private Integer id;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column event_tbl.name
     *
     * @mbg.generated Tue Dec 01 10:47:07 PST 2020
     */
    private String name;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column event_tbl.start_date
     *
     * @mbg.generated Tue Dec 01 10:47:07 PST 2020
     */
    private Date startDate;
    private Date endDate;
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column event_tbl.good_id
     *
     * @mbg.generated Tue Dec 01 10:47:07 PST 2020
     */
    private Integer goodId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column event_tbl.deal_price
     *
     * @mbg.generated Tue Dec 01 10:47:07 PST 2020
     */
    private BigDecimal dealPrice;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column event_tbl.id
     *
     * @return the value of event_tbl.id
     *
     * @mbg.generated Tue Dec 01 10:47:07 PST 2020
     */
    public Integer getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column event_tbl.id
     *
     * @param id the value for event_tbl.id
     *
     * @mbg.generated Tue Dec 01 10:47:07 PST 2020
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column event_tbl.name
     *
     * @return the value of event_tbl.name
     *
     * @mbg.generated Tue Dec 01 10:47:07 PST 2020
     */
    public String getName() {
        return name;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column event_tbl.name
     *
     * @param name the value for event_tbl.name
     *
     * @mbg.generated Tue Dec 01 10:47:07 PST 2020
     */
    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column event_tbl.start_date
     *
     * @return the value of event_tbl.start_date
     *
     * @mbg.generated Tue Dec 01 10:47:07 PST 2020
     */
    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column event_tbl.start_date
     *
     * @param startDate the value for event_tbl.start_date
     *
     * @mbg.generated Tue Dec 01 10:47:07 PST 2020
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column event_tbl.good_id
     *
     * @return the value of event_tbl.good_id
     *
     * @mbg.generated Tue Dec 01 10:47:07 PST 2020
     */
    public Integer getGoodId() {
        return goodId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column event_tbl.good_id
     *
     * @param goodId the value for event_tbl.good_id
     *
     * @mbg.generated Tue Dec 01 10:47:07 PST 2020
     */
    public void setGoodId(Integer goodId) {
        this.goodId = goodId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column event_tbl.deal_price
     *
     * @return the value of event_tbl.deal_price
     *
     * @mbg.generated Tue Dec 01 10:47:07 PST 2020
     */
    public BigDecimal getDealPrice() {
        return dealPrice;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column event_tbl.deal_price
     *
     * @param dealPrice the value for event_tbl.deal_price
     *
     * @mbg.generated Tue Dec 01 10:47:07 PST 2020
     */
    public void setDealPrice(BigDecimal dealPrice) {
        this.dealPrice = dealPrice;
    }
}