package cc.dobot.crtcpdemo;

import android.view.View;
import androidx.lifecycle.ViewModel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class TaskViewModel extends ViewModel {
    private Map<Integer, List<View>> taskBlocks = new HashMap<>();
    private Map<Integer, List<Integer>> taskElementTypes = new HashMap<>();
    private Map<Integer, String> taskPositions = new HashMap<>(); // Для сохранения позиций

    public List<View> getBlocksForTask(int index) {
        return taskBlocks.get(index);
    }

    public void addElementType(int taskIndex, int elementType) {
        List<Integer> types = taskElementTypes.get(taskIndex);
        if (types == null) {
            types = new ArrayList<>();
            taskElementTypes.put(taskIndex, types);
        }
        types.add(elementType);
    }

    public List<Integer> getElementTypes(int taskIndex) {
        return taskElementTypes.get(taskIndex);
    }

    public void setBlocksForTask(int index, List<View> views) {
        taskBlocks.put(index, views);
    }

    public void addBlock(int index, View view) {
        List<View> views = taskBlocks.get(index);
        if (views == null) {
            views = new java.util.ArrayList<>();
            taskBlocks.put(index, views);
        }
        views.add(view);
    }

    // Методы для сохранения позиций
    public void savePosition(int index, String positionData) {
        taskPositions.put(index, positionData);
    }

    public String getPosition(int index) {
        return taskPositions.get(index);
    }
}