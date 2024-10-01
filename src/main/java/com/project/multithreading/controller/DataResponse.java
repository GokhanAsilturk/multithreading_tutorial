package com.project.multithreading.controller;

import lombok.Builder;

@Builder
public class DataResponse<T> {
    public T data;
}
