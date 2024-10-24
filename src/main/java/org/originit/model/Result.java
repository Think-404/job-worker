package org.originit.model;

public class Result<T, E> {

    private T data;

    private E error;

    private Result() {
    }

    public static <T, E> Result<T, E> success(T data) {
        Result<T, E> result = new Result<>();
        result.data = data;
        return result;
    }

    public static <T, E> Result<T, E> error(E error) {
        Result<T, E> result = new Result<>();
        result.error = error;
        return result;
    }

    public T getData() {
        return data;
    }

    public E getError() {
        return error;
    }

    public boolean isSuccess() {
        return data != null;
    }

}
