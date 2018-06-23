/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.data.source.remote;

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.DataSource;
import com.google.common.base.Optional;
import com.google.gson.GsonBuilder;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Implementation of the data source that adds a latency simulating network.
 */
public class RemoteDataSource implements DataSource {

    private static RemoteDataSource INSTANCE;

    private static final int SERVICE_LATENCY_IN_MILLIS = 5000;

    private final static Map<String, Task> TASKS_SERVICE_DATA;

    private HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
    private OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();

    private RestApi restAPI;

    private static final String BASE_URL = ""; // TODO: base url for rest API calls

    {
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        okHttpClient.addInterceptor(loggingInterceptor);
//        okHttpClient.addInterceptor(chain -> { //TODO: add headers
//            Request request = chain.request().newBuilder().addHeader("API-KEY", "").build();
//            return chain.proceed(request);
//        });
        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()))
                .client(okHttpClient.build())
                .baseUrl(BASE_URL)
                .build();
        restAPI = retrofit.create(RestApi.class); // Use this retrofit's REST interface for remote API calls
    }

    static {
        TASKS_SERVICE_DATA = new LinkedHashMap<>(2);
        addTask("Build tower in Pisa", "Ground looks good, no foundation work required.");
        addTask("Finish bridge in Tacoma", "Found awesome girders at half the cost!");
    }

    public static RemoteDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RemoteDataSource();
        }
        return INSTANCE;
    }

    // Prevent direct instantiation.
    private RemoteDataSource() {
    }

    private static void addTask(String title, String description) {
        Task newTask = new Task(title, description);
        TASKS_SERVICE_DATA.put(newTask.getId(), newTask);
    }

    @Override
    public Flowable<List<Task>> getTasks() {
//        return restAPI.getTasks(); //TODO: use retrofit's rest interface
        return Flowable
                .fromIterable(TASKS_SERVICE_DATA.values())
                .delay(SERVICE_LATENCY_IN_MILLIS, TimeUnit.MILLISECONDS)
                .toList()
                .toFlowable();
    }

    @Override
    public Flowable<Optional<Task>> getTask(@NonNull String taskId) {
//        return restAPI.getTask(taskId); //TODO: use retrofit's rest interface
        final Task task = TASKS_SERVICE_DATA.get(taskId);
        if (task != null) {
            return Flowable.just(Optional.of(task)).delay(SERVICE_LATENCY_IN_MILLIS, TimeUnit.MILLISECONDS);
        } else {
            return Flowable.empty();
        }
    }

    @Override
    public void saveTask(@NonNull Task task) {
//        restAPI.saveTask(task); //TODO: use retrofit's rest interface
        TASKS_SERVICE_DATA.put(task.getId(), task);
    }

    @Override
    public void completeTask(@NonNull Task task) {
//        restAPI.completeTask(task); //TODO: use retrofit's rest interface
        Task completedTask = new Task(task.getTitle(), task.getDescription(), task.getId(), true);
        TASKS_SERVICE_DATA.put(task.getId(), completedTask);
    }

    @Override
    public void completeTask(@NonNull String taskId) {
        // Not required for the remote data source because the {@link Repository} handles
        // converting from a {@code taskId} to a {@link task} using its cached data.
    }

    @Override
    public void activateTask(@NonNull Task task) {
        Task activeTask = new Task(task.getTitle(), task.getDescription(), task.getId());
        TASKS_SERVICE_DATA.put(task.getId(), activeTask);
    }

    @Override
    public void activateTask(@NonNull String taskId) {
        // Not required for the remote data source because the {@link Repository} handles
        // converting from a {@code taskId} to a {@link task} using its cached data.
    }

    @Override
    public void clearCompletedTasks() {
        Iterator<Map.Entry<String, Task>> it = TASKS_SERVICE_DATA.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Task> entry = it.next();
            if (entry.getValue().isCompleted()) {
                it.remove();
            }
        }
    }

    @Override
    public void refreshTasks() {
        // Not required because the {@link Repository} handles the logic of refreshing the
        // tasks from all the available data sources.
    }

    @Override
    public void deleteAllTasks() {
        TASKS_SERVICE_DATA.clear();
    }

    @Override
    public void deleteTask(@NonNull String taskId) {
        TASKS_SERVICE_DATA.remove(taskId);
    }
}
