//package cc.dobot.crtcpdemo.message.product.cr;
//
//import android.os.Build;
//import android.util.Log;
//
//import com.xuhao.didi.core.pojo.OriginalData;
//
//import java.nio.charset.Charset;
//import java.nio.charset.StandardCharsets;
//
//import cc.dobot.crtcpdemo.message.base.BaseMessage;
//
//public class CRMessageUser extends BaseMessage {
//
//    private int index=0;
//    @Override
//    public void constructSendData() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            this.messageContent=("User("+index+")").getBytes(StandardCharsets.US_ASCII);
//            Log.d("CRMessageUser", "Выбраный индекс:" + index);
//        }else
//            this.messageContent=("User("+index+")").getBytes( Charset.forName("US-ASCII"));
//        this.messageStrContent=("User("+index+")");
//    }
//
//    @Override
//    public void transformReceiveData2Object(OriginalData data) {
//
//        this.messageContent=data.getBodyBytes();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            this.messageStrContent = new String(data.getBodyBytes(), StandardCharsets.US_ASCII);
//        }else
//            this.messageStrContent = new String(data.getBodyBytes(),Charset.forName("US-ASCII"));
//    }
//
//    public int getIndex() {
//        return index;
//    }
//
//    public void setIndex(int index) {
//        this.index = index;
//        constructSendData();
//    }
//}
package cc.dobot.crtcpdemo.message.product.cr;

import android.os.Build;

import com.xuhao.didi.core.pojo.OriginalData;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import cc.dobot.crtcpdemo.message.base.BaseMessage;

public class CRMessageUser extends BaseMessage {

    private int index = 0;
    private String table = ""; // Добавляем поле для хранения параметров таблицы

    @Override
    public void constructSendData() {
        String command;
        if (table.isEmpty()) {
            // Если таблица не задана, используем старый формат команды
            command = "User(" + index + ")";
        } else {
            // Если таблица задана, используем новый формат с параметрами
            command = "SetUser(" + index + "," + table + ")";
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.messageContent = command.getBytes(StandardCharsets.US_ASCII);
        } else {
            this.messageContent = command.getBytes(Charset.forName("US-ASCII"));
        }
        this.messageStrContent = command;
    }

    @Override
    public void transformReceiveData2Object(OriginalData data) {
        this.messageContent = data.getBodyBytes();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.messageStrContent = new String(data.getBodyBytes(), StandardCharsets.US_ASCII);
        } else {
            this.messageStrContent = new String(data.getBodyBytes(), Charset.forName("US-ASCII"));
        }
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
        constructSendData();
    }

    /**
     * Устанавливает параметры пользовательской системы координат
     * @param table Строка с параметрами в формате "{x,y,z,rx,ry,rz}"
     */
    public void setTable(String table) {
        this.table = table;
        constructSendData();
    }

    /**
     * Устанавливает параметры пользовательской системы координат из массива
     * @param coordinates Массив координат [x,y,z,rx,ry,rz]
     */
    public void setTable(double[] coordinates) {
        if (coordinates == null || coordinates.length != 6) {
            throw new IllegalArgumentException("Coordinates array must contain exactly 6 values");
        }
        this.table = String.format("{%f,%f,%f,%f,%f,%f}",
                coordinates[0], coordinates[1], coordinates[2],
                coordinates[3], coordinates[4], coordinates[5]);
        constructSendData();
    }

    public String getTable() {
        return table;
    }
}