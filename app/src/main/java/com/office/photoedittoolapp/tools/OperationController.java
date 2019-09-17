package com.office.photoedittoolapp.tools;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Bundle;

import com.office.photoedittoolapp.data.BitmapState;
import com.office.photoedittoolapp.data.EraseDrawContainer;
import com.office.photoedittoolapp.view.PhotoEditor;

import java.util.ArrayList;

public class OperationController {

    private static final String STATES_KEY = "STATES_KEY";
    private static final String UNDO_STATES_KEY = "UNDO_STATES_KEY";
    private static final String CURRENT_STATE_KEY = "CURRENT_STATE_KEY";

    public OperationController(OperationCallback operationCallback) {
        this.operationCallback = operationCallback;
    }

    private ArrayList<BitmapState> states = new ArrayList<>();
    private ArrayList<BitmapState> undoStates = new ArrayList<>();
    private BitmapState currentState = new BitmapState();
    private OperationCallback operationCallback;

    public void changeAdjust(int brightness, float contrast) {
        currentState.brightness = brightness - 255;
        currentState.contrast = contrast / 100;
        operationCallback.onBitmapStateChanged(currentState, true, false );
    }

    public void saveAdjustBitmapState(int brightness, float contrast) {
        states.add(currentState);
        BitmapState bitmapState = new BitmapState(currentState);
        bitmapState.brightness = brightness - 255;
        bitmapState.contrast = contrast / 100;
        currentState = bitmapState;
        operationCallback.onBitmapStateChanged(currentState, true, false );
    }

    public void rotateRight() {
        states.add(currentState);
        BitmapState state = new BitmapState(currentState);
        state.setRotate(-90);
        currentState = state;
        operationCallback.onBitmapStateChanged(currentState, true, false );
    }

    public void rotateLeft() {
        states.add(currentState);
        BitmapState state = new BitmapState(currentState);
        state.setRotate(90);
        currentState = state;
        operationCallback.onBitmapStateChanged(currentState, true, false );
    }

    public void flipVertical() {
        states.add(currentState);
        BitmapState state = new BitmapState(currentState);
        state.setFlipVertical(!state.isFlipVertical());
        currentState = state;
        operationCallback.onBitmapStateChanged(currentState, true, false );
    }

    public void flipHorizontal() {
        states.add(currentState);
        BitmapState state = new BitmapState(currentState);
        state.setFlipHorizontal(!state.isFlipHorizontal());
        currentState = state;
        operationCallback.onBitmapStateChanged(currentState, true, false );
    }

    public void eraseStateChanged(ArrayList<EraseDrawContainer> paths) {
        BitmapState state = new BitmapState(currentState);
        state.setPaths(paths);
        states.add(currentState);
        currentState = state;
        operationCallback.onBitmapStateChanged(currentState, true, false );
    }


    public void undo() {
        if (states.size() != 0) {
            undoStates.add(currentState);
            currentState = states.remove(states.size() - 1);
        }
        operationCallback.onBitmapStateChanged(currentState, true, false);
    }

    public void reundo(){
        if (undoStates.size() != 0){
            states.add(currentState);
            currentState = undoStates.remove(undoStates.size() - 1);
        }
        operationCallback.onBitmapStateChanged(currentState, false, true);
    }

    public void applyCrop(){
        BitmapState state = new BitmapState(currentState);
        state.clearPaths();
        state.dropRotate();
        currentState = state;
        states.clear();
        undoStates.clear();
        operationCallback.onBitmapStateChanged(currentState, false, false);
    }

    public BitmapState getCurrentState() {
        return currentState;
    }

    public void onSaveInstanceState(PhotoEditor.MyState bundle){
       bundle.currentState = currentState;
       bundle.states = states;
       bundle.undoStates = undoStates;
    }

    public void onRestoreInstanceState(PhotoEditor.MyState bundle){
        currentState = bundle.currentState;
        states = bundle.states;
        undoStates = bundle.undoStates;
        operationCallback.onBitmapStateChanged(currentState, false, false);
    }

    public interface OperationCallback{
        void onBitmapStateChanged(BitmapState state, boolean isUndo, boolean isReundo);
    }

}
