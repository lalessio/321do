package com.alessio.luca.b321do;

/**
 * Created by Luca on 21/10/2016.
 */

public class SortingOrder {
    public enum Order {NONE,DUEDATE,IMPORTANCE,CATEGORY};
    public enum Filter {NONE,TODAY,WITH_ATTACHMENT,ONLY_PLANNED,ONLY_EXPIRED,ONLY_COMPLETED,TOMORROW,NEXT7DAYS,WITH_SUB_ACTIVITIES}

    private final Order order;
    private final Filter filter;
    private final String searchParameter;
    private boolean tagCase; //just to make one last case works, i know it sucks but i don't have much time to think about a better solution

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
    public boolean isTagCase(){
        return tagCase;
    }

    public SortingOrder(){
        this.order = Order.NONE;
        this.filter = Filter.NONE;
        this.searchParameter = "";
        this.tagCase = false;
    }
    public SortingOrder(Order o, Filter f){
        this.order = o;
        this.filter = f;
        this.searchParameter = "";
        this.tagCase = false;
    }
    public SortingOrder(Order o, Filter f, String s){
        this.order = o;
        this.filter = f;
        this.searchParameter = s;
        this.tagCase = false;
    }
    public SortingOrder(String s, boolean b){
        this.order = Order.CATEGORY;
        this.filter = Filter.NONE;
        this.searchParameter = s;
        this.tagCase = b;
    }
}
