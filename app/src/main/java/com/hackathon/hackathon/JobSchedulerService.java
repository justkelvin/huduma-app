package com.hackathon.hackathon;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class JobSchedulerService extends JobService {
    private boolean jobWorking = false;
    public boolean jobCancel = false;
    private boolean notified = false;
    @Override
    public boolean onStartJob(final JobParameters parameters){
        jobWorking = true;


        // job logic

        runOnThread(parameters);
        jobFinished(parameters, false);

        return jobWorking;
    }

    @Override
    public boolean onStopJob(JobParameters parameters){
        jobCancel = true;
        // if the job is done, it does not need to be rescheduled and vice versa
        boolean reschedule = jobWorking;
        // let the system know the job has been finished
        jobFinished(parameters, reschedule);
        // let the system know if it needs to be rescheduled
        return reschedule;
    }

  //  public boolean checkIfTurn(){
       // if()
    //}

    public void runOnThread(final JobParameters parameters){
        new Thread(new Runnable() {
            @Override
            public void run() {
                // set the thread in the background
                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

                    // event listener for the database
                    FirebaseDatabase.getInstance().getReference("CurrentQueueInLine").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            // it is their turn
                            if((long)(dataSnapshot.getValue()) == parameters.getExtras().getLong("clientID")){
                                notifyClient();
                            }
                        }

                       @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                });
            }
        }).start();
    }

    public void notifyClient(){
        try {
            NotificationChannel channel = new NotificationChannel("Channel_1", "Notify", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notifyManager.createNotificationChannel(channel);
            Notification notify = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle("Your Position is Ready!").setContentText("Welcome and enjoy our service!")
                    .setChannelId("Channel_1").build();
            notifyManager.notify((int) (Math.random()*1001), notify);
            notified = true;
        }catch (Exception error){
            error.printStackTrace();
        }
    }
}

