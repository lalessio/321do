package com.alessio.luca.a321do;

/**
 * Created by Luca on 21/10/2016.
 */

public class SortingOrder {
    public enum Order {NONE,DUEDATE,IMPORTANCE,CATEGORY,ONLY_COMPLETED,ONLY_EXPIRED,TODAY,TOMORROW,NEXT7DAYS};
    private final Order order;
    private final String searchParameter;

    public Order getOrder() {
        return order;
    }
    public String getSearchParameter() {
        return searchParameter;
    }
    public boolean isSearchParameterSet(){
        return searchParameter != "";
    }

    public SortingOrder(){
        this.order = Order.NONE;
        this.searchParameter = "";
    }
    public SortingOrder(Order o){
        this.order = o;
        this.searchParameter = "";
    }
    public SortingOrder(Order o, String s){
        this.order = o;
        this.searchParameter = s;
    }
}
