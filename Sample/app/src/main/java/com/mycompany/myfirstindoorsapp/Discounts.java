package com.mycompany.myfirstindoorsapp;

/**
 * Created by wov on 07.02.17.
 */

public class Discounts {

    private static Discounts mInstance = null;

    private String token;
    private String userName;
    private String instaToken;
    private int currentPhoto;

    private Discounts(){
        this.token="eyJhbGciOiJIUzI1NiJ9.eyJ1IjoyfQ.TSovbMjK8OEKPfIShLnmW_Aah1Y-jGG7xU_C6fc_A9Y";
        this.instaToken="2961461944.792bc63.3c8204a5bfff4992be12e6af80162a9b";
        this.userName="told.ever";
        this.currentPhoto=0;
    }

    public static Discounts getInstance(){
        if(mInstance == null)
        {
            mInstance = new Discounts();
        }
        return mInstance;
    }

    public String getToken(){
        return token;
    }
    public int getCurrentPhoto(){
        return currentPhoto;
    }
    public String getUserName(){
        return userName;
    }
    public String getInstaToken(){
        return instaToken;
    }
    public void setToken(String newToken){
        this.token=newToken;
    }
    public void setCurrentPhoto(int newCurrentPhoto){
        this.currentPhoto=newCurrentPhoto;
    }
    public void setUserName(String newUserName){
        this.userName=newUserName;
    }
    public void setInstaToken(String newInstaToken){
        this.instaToken=newInstaToken ;
    }

}
