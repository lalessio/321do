package com.alessio.luca.a321do;

/**
 * Created by Luca on 21/10/2016.
 */

public class SortingOrder {
    public enum Order {NONE,DUEDATE,IMPORTANCE,CATEGORY};
    public enum Filter {NONE,TODAY,WITH_ATTACHMENT,ONLY_PLANNED,ONLY_EXPIRED,ONLY_COMPLETED,TOMORROW,NEXT7DAYS}

    private final Order order;
    private final Filter filter;
    private final String searchParameter;

    public Order getOrder() {
        return order;
    }
    public Filter getFilter() {
        return filter;
    }
    public String getSearchParameter() {
        return searchParameter;
    }
    public boolean isSearchParameterSet(){
        return searchParameter != "";
    }

    public SortingOrder(){
        this.order = Order.NONE;
        this.filter = Filter.NONE;
        this.searchParameter = "";
    }
    public SortingOrder(Order o, Filter f){
        this.order = o;
        this.filter = f;
        this.searchParameter = "";
    }
    public SortingOrder(Order o, Filter f, String s){
        this.order = o;
        this.filter = f;
        this.searchParameter = s;
    }
}
