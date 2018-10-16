package sk.tuke.smart.makac.helpers;

import java.util.List;

/**
 * Created by Jakub on 5.11.2017.
 *
 */

public final class SportActivities {

    /**
     * Returns MET value for an activity.
     * @param activityType - sport activity type (0 - running, 1 - walking, 2 - cycling)
     * @param speed - speed in m/s
     */
    public static double getMET(int activityType, Float speed){

        if (activityType == 0){
            switch ((int)Math.ceil(speed.doubleValue())){
                case 1:
                    return 23.0;
                case 4:
                    return 6.0;
                case 5:
                    return 8.3;
                case 6:
                    return 9.8;
                case 7:
                    return 11.0;
                case 8:
                    return 11.8;
                case 9:
                    return 12.8;
                case 10:
                    return 14.5;
                case 11:
                    return 16.0;
                case 12:
                    return 19.0;
                case 13:
                    return 19.8;
                default:
                    return speed * 1.535353535;
            }
        } else if (activityType == 1){
            switch ((int)Math.ceil(speed.doubleValue())){
                case 1:
                    return 2.0;
                case 2:
                    return 2.8;
                case 3:
                    return 3.1;
                case 4:
                    return 3.5;
                default:
                    return speed * 1.14;
            }
        } else if (activityType == 2){
            switch ((int)Math.ceil(speed.doubleValue())){
                case 10:
                    return 6.8;
                case 12:
                    return 8.0;
                case 14:
                    return 10.0;
                case 16:
                    return 12.8;
                case 18:
                    return 13.6;
                case 20:
                    return 15.8;
                default:
                    return speed * 0.744444444;
            }
        }
        return 0;
    }


    /**
     * Returns final calories computed from the data provided (returns value in kcal)
     * @param sportActivity - sport activity type (0 - running, 1 - walking, 2 - cycling)
     * @param weight - weight in kg
     * @param speedList - list of all speed values recorded (unit = m/s)
     * @param timeFillingSpeedListInHours - time of collecting speed list (duration of sport activity from first to last speedPoint in speedList)
     */
    public static double countCalories(int sportActivity, float weight, List<Float> speedList, double timeFillingSpeedListInHours) {

        int size = speedList.size();
        if(size == 0){
            return 0;
        }
        Float avgSpeed, count = 0f;

        for (Float speed : speedList) {
            count += speed;
        }
        avgSpeed = count / size ;

        if (sportActivity == 0){
            return getMET(0, avgSpeed) * weight * timeFillingSpeedListInHours;
        } else if (sportActivity == 1) {
            return getMET(1, avgSpeed) * weight * timeFillingSpeedListInHours;
        } else if (sportActivity == 2){
            return getMET(2, avgSpeed) * weight * timeFillingSpeedListInHours;
        }

        return 0.0;
    }
}
