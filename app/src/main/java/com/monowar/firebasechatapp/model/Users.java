package com.monowar.firebasechatapp.model;

/**
 * Created by kai_h on 23-Jul-17.
 */

public class Users {

    private String name, status, image, thumb_image, device_token;

    public Users() {

    }

    public Users(String name, String status, String image, String thumb_image, String device_token) {
        this.name = name;
        this.status = status;
        this.image = image;
        this.thumb_image = thumb_image;
        this.device_token = device_token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public String getDevice_token() {
        return device_token;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }
}
