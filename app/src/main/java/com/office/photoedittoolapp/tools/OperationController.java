package com.office.photoedittoolapp.tools;

import android.graphics.Bitmap;
import android.graphics.RectF;

import java.util.ArrayList;

public class OperationController {

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
        operationCallback.onOperationDone(currentState, true, false );
    }

    public void saveAdjustBitmapState(int brightness, float contrast) {
        states.add(currentState);
        BitmapState bitmapState = new BitmapState(currentState);
        bitmapState.brightness = brightness - 255;
        bitmapState.contrast = contrast / 100;
        currentState = bitmapState;
        operationCallback.onOperationDone(currentState, true, false );
    }

    public void rotateRight() {
        BitmapState state = new BitmapState(currentState);
        state.setRotate(-90);
        states.add(currentState);
        currentState = state;
        operationCallback.onOperationDone(currentState, true, false );
    }

    public void rotateLeft() {
        BitmapState state = new BitmapState(currentState);
        state.setRotate(90);
        states.add(currentState);
        currentState = state;
        operationCallback.onOperationDone(currentState, true, false );
    }

    public void flipVertical() {
        states.add(currentState);
        BitmapState state = new BitmapState(currentState);
        state.isFlipVertical = !currentState.isFlipVertical;
        currentState = state;
        operationCallback.onOperationDone(currentState, true, false );
    }

    public void flipHorizontal() {
        states.add(currentState);
        BitmapState state = new BitmapState(currentState);
        state.isFlipHorizontal = !currentState.isFlipHorizontal;
        currentState = state;
        operationCallback.onOperationDone(currentState, true, false );
    }

    public void eraseStateChanged(ArrayList<EraseDrawContainer> paths) {
        BitmapState state = new BitmapState(currentState);
        state.setPaths(paths);
        states.add(currentState);
        currentState = state;
        operationCallback.onOperationDone(currentState, true, false );
    }


    public void undo() {
        if (states.size() != 0) {
            undoStates.add(currentState);
            currentState = states.remove(states.size() - 1);
        }
        operationCallback.onOperationDone(currentState, true, false);
    }

    public void reundo(){
        if (undoStates.size() != 0){
            states.add(currentState);
            currentState = undoStates.remove(undoStates.size() - 1);
        }
        operationCallback.onOperationDone(currentState, false, true);
    }

    public void applyCrop(RectF crop, Bitmap bitmap){
        BitmapState state = new BitmapState(currentState);
        states.add(currentState);
        state.cropShape = crop;
        state.croppedBitmap = bitmap;
        state.clearPaths();
        currentState = state;
        operationCallback.onOperationDone(currentState, false, false);
    }

    public BitmapState getCurrentState() {
        return currentState;
    }

    public interface OperationCallback{
        void onOperationDone(BitmapState state, boolean isUndo, boolean isReundo);
    }

}
