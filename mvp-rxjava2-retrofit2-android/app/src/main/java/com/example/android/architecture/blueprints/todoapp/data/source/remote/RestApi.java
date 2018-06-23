package com.example.android.architecture.blueprints.todoapp.data.source.remote;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.pojo.Response;
import com.google.common.base.Optional;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Created by Basit Ali on 21-Jun-18.
 */

public interface RestApi {

    @GET("") //TODO: path
    Flowable<List<Task>> getTasks();

    @GET("")
    Flowable<Optional<Task>> getTask(@Path("id") String taskId);

    @POST("")
    Observable<Response<Task>> saveTask(@Body Task task);

    @PUT("")
    Observable<Response<Task>> completeTask(@Body Task task);

}
