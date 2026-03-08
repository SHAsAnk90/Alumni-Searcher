package com.shashank.alumni.dto;

import java.util.List;

public class ApiResponse<T> {
    private String status;
    private List<T> data;

    public ApiResponse(String status, List<T> data) {
        this.status = status;
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
