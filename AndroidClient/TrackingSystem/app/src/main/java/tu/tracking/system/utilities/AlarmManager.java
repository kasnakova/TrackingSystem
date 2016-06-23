package tu.tracking.system.utilities;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;

/**
 * Created by Liza on 19.6.2016 г..
 */
public class AlarmManager {
    private static int originalRingerMode;
    private static MediaPlayer thePlayer;
    private static AudioManager audioManager;

    public static void start(Context context){
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        originalRingerMode = audioManager.getRingerMode();
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        audioManager.setStreamVolume(
                AudioManager.STREAM_RING,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_RING),
                0);
        thePlayer = MediaPlayer.create(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
        thePlayer.start();
    }

    public static void stop(){
        thePlayer.stop();
        audioManager.setRingerMode(originalRingerMode);
    }
}
