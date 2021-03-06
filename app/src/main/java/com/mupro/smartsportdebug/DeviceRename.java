package com.mupro.smartsportdebug;

import java.util.Random;

/**
 * Created by Administrator on 2016/7/8.
 */
public class DeviceRename {
    private static String PREFIX = "EPT";
    private static String SUFFIX = "L";
    private static int REN_TAB[] = {8,0,10,6,3,9,11,2,5,4,1,7};
    static String getNewName(String address){
        String midName = address.replace(":","");
        //int[] images = get_RandomID(midName.length());
        StringBuilder builder = new StringBuilder();
        for(int i=0;i<REN_TAB.length;i++){
            builder.append(midName.charAt(REN_TAB[i]));
        }
        String newMidName = builder.toString();
        return PREFIX + newMidName + SUFFIX;
    }

    static private int[] get_RandomID(int size)
    {
        int id[] = new int[size];
        int image_id[] = new int[size];
        for(int i=0;i<size;i++)
        {
            id[i]=i;
        }
        int last=size-1;
        Random r = new Random();
        int temp;
        for(int i=0;i<size-1;i++)
        {
            temp= Math.abs(r.nextInt()%last);
            image_id[i]=id[temp];
            id[temp]=id[last];
            id[last]=image_id[i];
            last--;
        }
        image_id[size-1]=id[0];
        return image_id;
    }


}
