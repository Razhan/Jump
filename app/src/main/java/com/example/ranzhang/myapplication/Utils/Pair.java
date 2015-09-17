package com.example.ranzhang.myapplication.Utils;

/**
 * Created by Ran on 9/17/2015.
 */
public class Pair<L extends Comparable<L>, R extends Comparable<R>> implements Comparable<Pair<L, R>> {
    private L l;
    private R r;
    public Pair(L l, R r){
        this.l = l;
        this.r = r;
    }
    public L getFirstKey(){ return l; }
    public R getSecondKey(){ return r; }
    public void setFirstKey(L l){ this.l = l; }
    public void setSecondKey(R r){ this.r = r; }

    @Override
    public int compareTo(Pair<L, R> other) {
        int cmp = this.getFirstKey().compareTo(other.getFirstKey());
        return cmp;
    }
}